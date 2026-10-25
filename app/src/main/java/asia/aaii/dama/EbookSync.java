package asia.aaii.dama;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/** Bounded, opt-in DhammaDownload book INDEX refresh (metadata only, no PDF download).
 * Source pages may be unavailable or may change; errors and source counts are reported.
 * Preserves user's offline book catalogue regardless of remote sync results.
 */
public final class EbookSync {
  public interface Listener {
    void onProgress(int checked,int total,int added,int errors,String page);
    void onDone(int added,int checked,int errors,String diagnostics);
  }
  public static final String CACHE_NAME="dama_ebook_catalog_updates.json";
  private static final String INDEX="https://www.dhammadownload.com/eBook-Myanmar.htm";
  private static final Pattern ANCHOR=Pattern.compile("(?is)<a\\b([^>]*?)>(.*?)</a\\s*>");
  private static final Pattern HREF=Pattern.compile("(?is)\\bhref\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))");
  private static final Pattern TAGS=Pattern.compile("(?s)<[^>]+>");
  private static final int MAX_PAGES=12;
  private EbookSync(){}

  public static JSONArray readSaved(Context ctx){
    File file=new File(ctx.getFilesDir(),CACHE_NAME);
    if(!file.isFile())return new JSONArray();
    try(FileInputStream stream=new FileInputStream(file);ByteArrayOutputStream buf=new ByteArrayOutputStream()){
      byte[] chunk=new byte[8192];int n;
      while((n=stream.read(chunk))!=-1){
        if(buf.size()+n>6_000_000)throw new IOException("Ebook cache limit");
        buf.write(chunk,0,n);
      }
      return new JSONArray(buf.toString("UTF-8"));
    }catch(Exception e){android.util.Log.w("DAMA_EBOOK","Cache read failed",e);return new JSONArray();}
  }

  private static String htmlText(String html){
    return TAGS.matcher(html).replaceAll(" ").replace("&nbsp;"," ").replace("&amp;","&")
      .replace("&quot;","\"").replace("&#39;","'").replaceAll("\\s+"," ").trim();
  }
  private static URL vetted(URL base,String raw) throws Exception{
    String href=raw.replace("&amp;","&").trim().replace(" ","%20");
    URL dst=new URL(base,href);
    String host=dst.getHost().toLowerCase(Locale.ROOT);
    if(!"https".equalsIgnoreCase(dst.getProtocol())||
      !(host.equals("www.dhammadownload.com")||host.equals("dhammadownload.com")))
      throw new IOException("Untrusted source");
    return dst;
  }
  private static String fetch(URL url)throws Exception{
    HttpURLConnection c=(HttpURLConnection)url.openConnection();
    c.setConnectTimeout(13000);c.setReadTimeout(23000);
    c.setRequestProperty("User-Agent","Mozilla/5.0 (Android; DAMA-Library/13.0)");
    c.setRequestProperty("Accept","text/html,application/xhtml+xml");
    c.setRequestProperty("Accept-Encoding","identity");
    try{
      if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
      URL redirected=c.getURL();
      if(!(redirected.getHost().equalsIgnoreCase("www.dhammadownload.com")||redirected.getHost().equalsIgnoreCase("dhammadownload.com"))||
        !redirected.getProtocol().equalsIgnoreCase("https"))
        throw new IOException("Publisher redirect blocked");
      ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] temp=new byte[16384];
      try(InputStream in=c.getInputStream()){int n;while((n=in.read(temp))!=-1){
        if(b.size()+n>5_000_000)throw new IOException("HTML exceeds 5 MB");
        b.write(temp,0,n);
      }}
      String type=c.getContentType();String charset="UTF-8";
      if(type!=null&&type.toLowerCase(Locale.ROOT).contains("charset=")){
        charset=type.replaceAll("(?i).*charset=\\s*([A-Za-z0-9_-]+).*","$1");
      }
      try{return b.toString(charset);}catch(Exception ignored){return b.toString("UTF-8");}
    }finally{c.disconnect();}
  }
  private static LinkedHashMap<String,String> anchors(String html,URL base,String suffix){
    LinkedHashMap<String,String> found=new LinkedHashMap<>();
    Matcher m=ANCHOR.matcher(html);
    while(m.find()){
      Matcher h=HREF.matcher(m.group(1));if(!h.find())continue;
      String raw=h.group(1)!=null?h.group(1):h.group(2)!=null?h.group(2):h.group(3);
      if(raw==null)continue;
      try{
        URL target=vetted(base,raw);
        String path=target.getPath().toLowerCase(Locale.ROOT);
        if("document".equals(suffix)){
          if(!path.matches("(?s).*\\.(pdf|doc|docx)$"))continue;
        }else{
          if(!path.matches("(?s).*\\.html?$"))continue;
          if(!path.contains("ebook")&&!path.contains("e-book")&&!path.contains("pitaka")&&!path.contains("book"))continue;
          if(target.toString().equalsIgnoreCase(INDEX))continue;
        }
        found.putIfAbsent(target.toString(),htmlText(m.group(2)));
      }catch(Exception ignored){}
    }
    return found;
  }
  private static JSONObject record(String link,String title,String source,String pageLabel){
    JSONObject j=new JSONObject();
    String path;try{path=new URL(link).getPath();}catch(Exception e){path=link;}
    String basename=path.substring(path.lastIndexOf('/')+1).replace("%20"," ").replace('_',' ');
    String text=(title.length()>3?title:basename);
    String both=(text+" "+pageLabel).toLowerCase(Locale.ROOT);
    String category=both.contains("ပဋ္ဌာန်း")||both.contains("patthana")?"ပဋ္ဌာန်း":
      both.contains("အဘိဓမ္မာ")?"အဘိဓမ္မာ":
      both.contains("ဝိပဿနာ")?"ဝိပဿနာ":
      both.contains("ပါဠိ")?"ပါဠိ":"ဓမ္မစာအုပ်";
    try{
      j.put("id","SYNC_"+Integer.toHexString(link.hashCode()));
      j.put("title",text.length()>170?text.substring(0,170):text);
      j.put("author","မူရင်းစာမျက်နှာမှ စစ်ဆေးရန်");
      j.put("author_type","collection");
      j.put("category",category);
      j.put("format",path.toLowerCase(Locale.ROOT).endsWith(".pdf")?"PDF":"DOC");
      j.put("url",link);j.put("listing_url",source);
      j.put("link_status","not_live_checked");j.put("source","dhammadownload_metadata_sync");
    }catch(Exception ignored){}
    return j;
  }
  private static void save(Context ctx,JSONArray rows)throws Exception{
    File file=new File(ctx.getFilesDir(),CACHE_NAME),tmp=new File(ctx.getFilesDir(),CACHE_NAME+".part");
    try(FileOutputStream out=new FileOutputStream(tmp)){
      out.write(rows.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
      out.getFD().sync();
    }
    if(!tmp.renameTo(file))throw new IOException("Metadata cache save failed");
  }
  public static void run(Context ctx,Set<String> existing,Listener listener){
    Handler main=new Handler(Looper.getMainLooper());
    new Thread(()->{
      int failed=0,checked=0;
      ArrayList<String> errors=new ArrayList<>();
      LinkedHashMap<String,JSONObject> additions=new LinkedHashMap<>();
      JSONArray old=readSaved(ctx);
      for(int i=0;i<old.length();i++){
        JSONObject j=old.optJSONObject(i);
        if(j!=null && j.optString("url").startsWith("https://"))
          additions.putIfAbsent(j.optString("url"),j);
      }
      LinkedHashMap<String,String> pages=new LinkedHashMap<>();
      pages.put(INDEX,"eBook in Myanmar");
      try{
        String index=fetch(new URL(INDEX));
        LinkedHashMap<String,String> child=anchors(index,new URL(INDEX),"page");
        for(Map.Entry<String,String> e:child.entrySet()){
          if(pages.size()>=MAX_PAGES)break;
          pages.putIfAbsent(e.getKey(),e.getValue());
        }
        // Root index contains direct books as well, never claim page count is exhaustive.
        for(Map.Entry<String,String> e:anchors(index,new URL(INDEX),"document").entrySet())
          if(!existing.contains(e.getKey()))additions.putIfAbsent(e.getKey(),record(e.getKey(),e.getValue(),INDEX,"eBook in Myanmar"));
      }catch(Exception e){
        failed++;errors.add("Index: "+e.getClass().getSimpleName()+" "+e.getMessage());
      }
      int total=pages.size();
      for(Map.Entry<String,String> page:pages.entrySet()){
        // Index may be re-fetched to keep parsing code simple.
        try{
          URL base=new URL(page.getKey());String html=fetch(base);
          for(Map.Entry<String,String> e:anchors(html,base,"document").entrySet()){
            if(!existing.contains(e.getKey()))
              additions.putIfAbsent(e.getKey(),record(e.getKey(),e.getValue(),page.getKey(),page.getValue()));
          }
        }catch(Exception e){failed++;if(errors.size()<5)errors.add(page.getKey()+": "+e.getMessage());}
        checked++;
        final int done=checked,totalPages=total,errorCount=failed,newCount=additions.size()-old.length();
        final String url=page.getKey();
        main.post(()->listener.onProgress(done,totalPages,Math.max(0,newCount),errorCount,url));
      }
      int totalAdded=0;
      JSONArray complete=new JSONArray();
      for(Map.Entry<String,JSONObject> entry:additions.entrySet()){
        complete.put(entry.getValue());
        if(!existing.contains(entry.getKey()))totalAdded++;
      }
      try{save(ctx,complete);}catch(Exception e){failed++;errors.add("Save: "+e.getMessage());}
      final int finalAdded=totalAdded,finalDone=checked,finalFailed=failed;
      String report=errors.isEmpty()?"Limited publisher source pages checked":
        android.text.TextUtils.join("\n",errors);
      main.post(()->listener.onDone(finalAdded,finalDone,finalFailed,report));
    },"DAMA-Ebook-Metadata-Sync").start();
  }
}
