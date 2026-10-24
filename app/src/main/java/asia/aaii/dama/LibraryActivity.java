package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.security.MessageDigest;
import java.net.*;

/** DAMA LIBRARY: offline BOOK LINK catalog, consent-based PDF downloads, native PDF reading. */
public class LibraryActivity extends Activity {
    static final int BG=0xff041e1c,DEEP=0xff001613,CARD=0xff11362e,GOLD=0xffe4b767,IVORY=0xfffff8e7,MUTED=0xffa6b3a5;
    final ArrayList<Book> books=new ArrayList<>(),visible=new ArrayList<>();
    Set<String> favorites=new HashSet<>(); SharedPreferences prefs;
    LinearLayout page,list,main; TextView counter; EditText search;
    String query="",category="အားလုံး",authorFilter=""; boolean favoritesOnly=false,offlineOnly=false;int rendered=0;
    final String[] categories={"အားလုံး","ပဋ္ဌာန်း","အဘိဓမ္မာ","ပိဋကတ်သုံးပုံ","ဝိပဿနာ","ဝန်ကြီးဌာန","ပရိတ်/ဝတ်ရွတ်","ပါဠိ","ဓမ္မစာအုပ်"};
    static class Book {String title,author,category,format,url,listUrl, status,id;
        Book(JSONObject x){title=x.optString("title","စာအုပ်"); author=x.optString("author", "အမည်မသိ");category=x.optString("category","ဓမ္မစာအုပ်");format=x.optString("format","PDF");url=x.optString("url");listUrl=x.optString("listing_url");status=x.optString("link_status");id=x.optString("id",Integer.toHexString(url.hashCode()));}
        boolean pdf(){return format.equalsIgnoreCase("pdf") || url.toLowerCase(Locale.ROOT).contains(".pdf");}
    }
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} LinearLayout vert(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);return x;}LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(0);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    GradientDrawable shape(int c,int r,int border){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));if(border!=0)g.setStroke(dp(1),border);return g;}
    TextView t(String s,int size,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextColor(color);x.setTextSize(size);x.setGravity(Gravity.CENTER_VERTICAL);if(bold)x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return x;}
    TextView btn(String text,Runnable fn){TextView b=t(text,12,GOLD,true);b.setPadding(dp(10),dp(9),dp(10),dp(9));b.setGravity(Gravity.CENTER);b.setBackground(shape(CARD,12,0xff856d45));b.setOnClickListener(v->fn.run());return b;}
    void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    JSONArray load(String p){try(InputStream in=getAssets().open(p)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[65536];int n;while((n=in.read(b))>0)out.write(b,0,n);return new JSONArray(out.toString("UTF-8"));}catch(Exception e){return new JSONArray();}}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(DEEP);getWindow().setNavigationBarColor(DEEP);prefs=getSharedPreferences("dama_books",0);favorites=new HashSet<>(prefs.getStringSet("saved",new HashSet<>())); LinkedHashMap<String,Book> uniq=new LinkedHashMap<>(); for(String f:new String[]{"seed_books.json","books_catalog.json"}){JSONArray x=load(f);for(int i=0;i<x.length();i++){JSONObject j=x.optJSONObject(i);if(j==null)continue;Book v=new Book(j);if(v.url.startsWith("http"))uniq.putIfAbsent(v.url,v);}}books.addAll(uniq.values());draw();}
    void draw(){main=vert();main.setBackgroundColor(BG);setContentView(main);
        LinearLayout hdr=row();hdr.setPadding(dp(14),dp(14),dp(14),dp(10));hdr.setBackgroundColor(DEEP);TextView back=t("‹",32,GOLD,true);hdr.addView(back,new LinearLayout.LayoutParams(dp(43),dp(43)));back.setOnClickListener(v->finish());LinearLayout title=vert();title.addView(t("DAMA LIBRARY",22,GOLD,true));title.addView(t("ဓမ္မစာအုပ်များ • Offline Catalog",10,MUTED,false));hdr.addView(title,new LinearLayout.LayoutParams(0,-2,1));main.addView(hdr);
        ScrollView scroll=new ScrollView(this);main.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));page=vert();page.setPadding(dp(12),dp(14),dp(12),dp(12));scroll.addView(page);
        android.widget.FrameLayout hero=new android.widget.FrameLayout(this);hero.setBackground(shape(CARD,20,GOLD));hero.setClipToOutline(true);ImageView cover=new ImageView(this);cover.setImageResource(R.drawable.radio_hero);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);hero.addView(cover,new android.widget.FrameLayout.LayoutParams(-1,dp(157)));LinearLayout words=vert();words.setPadding(dp(20),dp(20),dp(6),dp(9));words.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xe900201b,0x9000201b,0x00102018}));words.addView(t("DAMA LIBRARY",24,GOLD,true));words.addView(t("ဓမ္မစာအုပ်များ စုစည်းရာ",15,IVORY,true));words.addView(t("Read • Learn • Keep",10,IVORY,false));hero.addView(words,new android.widget.FrameLayout.LayoutParams(-1,-1));page.addView(hero,new LinearLayout.LayoutParams(-1,dp(157)));
        page.addView(t("စာအုပ်စာရင်း : "+books.size()+" • Metadata offline • PDF online",11,MUTED,false));
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(DEEP);search.setHintTextColor(0xff777777);search.setTextSize(14);search.setPadding(dp(13),dp(5),dp(13),dp(5));search.setBackground(shape(0xfffcf8ec,12,0));search.setHint("စာအုပ် / ဆရာတော် / PDF ရှာရန်…");search.setText(query);page.addView(search,new LinearLayout.LayoutParams(-1,dp(49)));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){query=s.toString();filter();}public void afterTextChanged(Editable e){}});
        page.addView(t("ဆရာတော် / စာရေးဆရာများ",15,GOLD,true));
        HorizontalScrollView authorscroll=new HorizontalScrollView(this);authorscroll.setHorizontalScrollBarEnabled(false);LinearLayout authorrow=row();
        LinkedHashMap<String,Integer> counts=new LinkedHashMap<>();for(Book book:books){if(!book.author.isEmpty())counts.put(book.author,counts.getOrDefault(book.author,0)+1);}
        ArrayList<String> persons=new ArrayList<>(counts.keySet());persons.sort((a,b)->Integer.compare(counts.get(b),counts.get(a)));
        int personCount=0;for(String name:persons){if(personCount++>=26)break;LinearLayout one=vert();one.setGravity(Gravity.CENTER);one.setPadding(dp(4),dp(5),dp(4),dp(5));TextView symbol=t("☸",23,GOLD,true);symbol.setGravity(Gravity.CENTER);symbol.setBackground(shape(CARD,23,0xff986f37));one.addView(symbol,new LinearLayout.LayoutParams(dp(45),dp(45)));TextView who=t(name,10,IVORY,false);who.setGravity(Gravity.CENTER);who.setMaxLines(2);one.addView(who,new LinearLayout.LayoutParams(dp(95),dp(39)));authorrow.addView(one);one.setOnClickListener(v->{authorFilter=authorFilter.equals(name)?"":name;filter();});}
        authorscroll.addView(authorrow);page.addView(authorscroll);
        LinearLayout tabs=row();tabs.setPadding(0,dp(7),0,dp(7));tabs.addView(btn("♡ Favorites",()->{favoritesOnly=!favoritesOnly;if(favoritesOnly)offlineOnly=false;filter();}),new LinearLayout.LayoutParams(0,-2,1));tabs.addView(btn("↓ Downloads",()->{offlineOnly=!offlineOnly;if(offlineOnly)favoritesOnly=false;filter();}),new LinearLayout.LayoutParams(0,-2,1));page.addView(tabs);
        HorizontalScrollView scroller=new HorizontalScrollView(this);scroller.setHorizontalScrollBarEnabled(false);LinearLayout chips=row();for(String c:categories){TextView x=btn(c,()->{category=c;filter();});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,dp(6),0);chips.addView(x,p);}scroller.addView(chips);page.addView(scroller);
        counter=t("",13,GOLD,true);counter.setPadding(dp(2),dp(13),0,dp(9));page.addView(counter);
        list=vert();page.addView(list);
        page.addView(btn("မူရင်း DhammaDownload စာကြည့်တိုက် ↗",()->open("https://www.dhammadownload.com/eBook-Myanmar.htm")));
        TextView rights=t("စာအုပ်လင့်ခ်များကို Offline သိမ်းထားသည်။ PDF ဖိုင်များကို ကိုယ်တိုင် Download လုပ်မှသာ Offline ဖတ်နိုင်သည်။ မူရင်းရေးသားသူများ၏ မူပိုင်ခွင့်ကို လေးစားပါ။",11,MUTED,false);rights.setPadding(0,dp(12),0,0);page.addView(rights);
        filter();
    }
    void filter(){if(list==null)return;visible.clear();String q=query.toLowerCase(Locale.ROOT).trim();for(Book b:books){if(!category.equals("အားလုံး")&&!b.category.contains(category))continue;if(!authorFilter.isEmpty()&&!b.author.equals(authorFilter))continue;if(favoritesOnly&&!favorites.contains(b.id))continue;if(offlineOnly&&!local(b).isFile())continue;if(!q.isEmpty()&&!(b.title+" "+b.author+" "+b.category).toLowerCase(Locale.ROOT).contains(q))continue;visible.add(b);}list.removeAllViews();rendered=0;counter.setText("စာအုပ် "+visible.size()+" အုပ် • "+category+(authorFilter.isEmpty()?"":" • "+authorFilter));showMore();}
    void showMore(){int end=Math.min(visible.size(),rendered+35);for(int i=rendered;i<end;i++){Book b=visible.get(i);LinearLayout line=row();line.setPadding(dp(8),dp(8),dp(8),dp(8));line.setBackground(shape(CARD,13,0));LinearLayout.LayoutParams itemParams=new LinearLayout.LayoutParams(-1,-2);itemParams.setMargins(0,0,0,dp(9));
        android.widget.FrameLayout icon=new android.widget.FrameLayout(this);icon.setBackground(shape(new int[]{0xff70572f,0xff8b3f32,0xff28594c,0xff3e5260}[i%4],8,GOLD));TextView glyph=t("☸",31,GOLD,true);glyph.setGravity(Gravity.CENTER);icon.addView(glyph,new android.widget.FrameLayout.LayoutParams(-1,-1));line.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(77)));
        LinearLayout txt=vert();txt.setPadding(dp(12),dp(1),dp(5),0);TextView ttl=t(b.title,13,IVORY,true);ttl.setMaxLines(2);txt.addView(ttl);TextView auth=t(b.author,11,MUTED,false);auth.setMaxLines(1);txt.addView(auth);txt.addView(t(b.format+" · "+b.category+(local(b).exists()?" · Offline ✓":""),10,GOLD,false));line.addView(txt,new LinearLayout.LayoutParams(0,-2,1));TextView open=t("›",28,GOLD,true);line.addView(open);list.addView(line,itemParams);line.setOnClickListener(v->details(b));}rendered=end;
        if(rendered<visible.size()){TextView more=btn("နောက်ထပ် စာအုပ်များ "+(visible.size()-rendered)+" ›",()->{list.removeViewAt(list.getChildCount()-1);showMore();});list.addView(more);}}
    File local(Book b){File dir=new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"DAMA-Library");if(!dir.exists())dir.mkdirs();return new File(dir,b.id+".pdf");}
    void details(Book b){String fav=favorites.contains(b.id)?"♥ Favorites မှ ဖယ်ရှား":"♡ Favorites ထဲသိမ်း";String[] options={"📖 Online PDF / မူရင်းဖိုင်",b.pdf()?"↓ PDF Download (Offline)":"↗ မူရင်းစာမျက်နှာ",local(b).exists()?"📚 Offline PDF ဖတ်ရန်":"📚 PDF မသိမ်းရသေး",fav,"↗ မူရင်း Website"};new AlertDialog.Builder(this).setTitle(b.title).setMessage(b.author+"\n"+b.category+" · "+b.format+"\n"+b.status).setItems(options,(d,i)->{if(i==0)open(b.url);else if(i==1){if(b.pdf())download(b);else open(b.url);}else if(i==2){if(local(b).exists())read(b);else msg("Download မလုပ်ရသေးပါ");}else if(i==3){if(!favorites.add(b.id))favorites.remove(b.id);prefs.edit().putStringSet("saved",new HashSet<>(favorites)).apply();filter();}else open(b.listUrl.isEmpty()?b.url:b.listUrl);}).show();}
    void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ex){msg("Link ဖွင့်မရပါ");}}
    void read(Book b){Intent i=new Intent(this,PdfReaderActivity.class);i.putExtra("path",local(b).getAbsolutePath());i.putExtra("title",b.title);startActivity(i);}
    void download(Book b){if(!b.pdf()){open(b.url);return;}if(local(b).exists()){read(b);return;}
        new AlertDialog.Builder(this).setTitle("Download for Offline Reading").setMessage("စာအုပ်ကို မူရင်း Website မှ ကိုယ်ပိုင်ဖုန်းသို့ Download လုပ်ပါမည်။ ဖိုင်ကြီးနိုင်ပြီး မူရင်းထုတ်ဝေသူ၏ အသုံးပြုခွင့်စည်းမျဉ်းများကို လိုက်နာပါ။").setNegativeButton("Cancel",null).setPositiveButton("Download",(d,w)->{msg("PDF Download စတင်ပါပြီ");new Thread(()->{File dst=local(b),tmp=new File(dst.getPath()+".part");String err=null;
            try{URL u=new URL(b.url);if(!u.getProtocol().equals("https"))throw new IOException("HTTPS required");HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(13000);c.setReadTimeout(30000);c.setInstanceFollowRedirects(true);if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());if(c.getContentLengthLong()>100L*1024*1024)throw new IOException("PDF >100MB");try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(tmp)){byte[] buf=new byte[16384];int n;long count=0;while((n=in.read(buf))!=-1){count+=n;if(count>100L*1024*1024)throw new IOException("Too large");out.write(buf,0,n);}if(count<500)throw new IOException("Too small");}try(FileInputStream f=new FileInputStream(tmp)){byte[] magic=new byte[5];if(f.read(magic)!=5||!Arrays.equals(magic,"%PDF-".getBytes(StandardCharsets.US_ASCII)))throw new IOException("Not a PDF");}if(!tmp.renameTo(dst))throw new IOException("Cannot save file");}catch(Exception e){tmp.delete();err=e.getMessage();}
            final String error=err;runOnUiThread(()->{if(error==null){msg("PDF Download ပြီးပါပြီ");filter();read(b);}else msg("Download မအောင်မြင်: "+error);});}).start();}).show();}
}