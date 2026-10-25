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
    LinearLayout page,list,main; ScrollView libraryScroll; TextView counter,ebookSyncLabel; ProgressBar ebookSyncProgress;
    EditText search;
    volatile boolean syncingBooks=false;
    int ebookSyncDone=0,ebookSyncTotal=1;
    String sayadawKey="",query="",category="အားလုံး",authorFilter="",fileFilter="ALL",statusFilter="ALL",sortMode="DEFAULT"; boolean favoritesOnly=false,offlineOnly=false;int rendered=0;
    final String[] categories={"အားလုံး","ပဋ္ဌာန်း","အဘိဓမ္မာ","ပိဋကတ်သုံးပုံ","ဝိပဿနာ","ဝန်ကြီးဌာန","ပရိတ်/ဝတ်ရွတ်","ပါဠိ","ဓမ္မစာအုပ်"};
    static class Book {String title,author,category,format,url,listUrl, status,id,authorType;
        Book(JSONObject x){title=x.optString("title","စာအုပ်").replaceAll("\\s+"," ").trim(); author=x.optString("author", "အမည်မသိ").replaceAll("\\s+"," ").trim();category=x.optString("category","ဓမ္မစာအုပ်");format=x.optString("format","PDF");url=x.optString("url");listUrl=x.optString("listing_url");status=x.optString("link_status");id=x.optString("id",Integer.toHexString(url.hashCode()));authorType=x.optString("author_type","person_label");}
        boolean isCollection(){return "collection".equals(authorType);}
        boolean pdf(){return Uri.parse(url).getPath()!=null && Uri.parse(url).getPath().toLowerCase(Locale.ROOT).endsWith(".pdf");}
    }
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} LinearLayout vert(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);return x;}LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(0);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    GradientDrawable shape(int c,int r,int border){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));if(border!=0)g.setStroke(dp(1),border);return g;}
    TextView t(String s,int size,int color,boolean bold){TextView x=new TextView(this);x.setText(s);x.setTextColor(color);x.setTextSize(size);x.setGravity(Gravity.CENTER_VERTICAL);if(bold)x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return x;}
    TextView btn(String text,Runnable fn){TextView b=t(text,12,GOLD,true);b.setPadding(dp(10),dp(9),dp(10),dp(9));b.setGravity(Gravity.CENTER);b.setBackground(shape(CARD,12,0xff856d45));PremiumMedia.tap(b,fn);return b;}
    void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    void authorProgressUI(){
        int linkedAuthor=0,linkedCat=0,persons=0;LinkedHashSet<String> allPeople=new LinkedHashSet<>(),allCollections=new LinkedHashSet<>();
        for(Book b:books){
            if(!b.author.trim().isEmpty())linkedAuthor++;
            if(!b.category.trim().isEmpty())linkedCat++;
            if(b.isCollection())allCollections.add(b.author); else {allPeople.add(b.author);persons++;}
        }
        LinearLayout frame=vert();frame.setPadding(dp(13),dp(12),dp(13),dp(12));frame.setBackground(shape(CARD,16,0xff947243));
        TextView heading=t("AUTHOR × CATEGORY LINKING",13,GOLD,true);frame.addView(heading);
        TextView note=t("စာအုပ် "+books.size()+" အုပ်၏ Index ချိတ်ဆက်မှု",11,IVORY,false);frame.addView(note);
        addLinkBar(frame,"စာရေးသူ / စာအုပ်စု အမည်ချိတ်ထားမှု",linkedAuthor,books.size());
        addLinkBar(frame,"Category ချိတ်ထားမှု",linkedCat,books.size());
        addLinkBar(frame,"စာရေးသူအမည် အောက်ရှိစာအုပ်",persons,books.size());
        frame.addView(t("ဆရာတော်အမည် "+allPeople.size()+" ခု · ကျမ်းစုအုပ်စု "+allCollections.size()+" ခု",11,GOLD,true));
        frame.addView(t("Metadata ချိတ်ဆက်မှုသာ ဖြစ်သည်။ စာရေးသူအစစ်နှင့် မူရင်း PDF Link အလုပ်လုပ်မှုကို သီးခြားအတည်ပြုရန် လိုပါသည်။",9,MUTED,false));
        LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2);margin.setMargins(0,dp(12),0,dp(8));page.addView(frame,margin);
    }
    void addLinkBar(LinearLayout into,String name,int count,int total){
        LinearLayout line=row();line.setPadding(0,dp(7),0,0);line.addView(t(name,10,IVORY,false),new LinearLayout.LayoutParams(0,-2,1));line.addView(t(count+" / "+total,10,GOLD,true));into.addView(line);
        ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(Math.max(1,total));bar.setProgress(count);
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffe1b970));bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff27443a));
        LinearLayout.LayoutParams param=new LinearLayout.LayoutParams(-1,dp(6));param.setMargins(0,dp(4),0,dp(5));into.addView(bar,param);
    }
    void openAuthorDirectory(){startActivityForResult(new Intent(this,AuthorDirectoryActivity.class),406);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==406 && resultCode==RESULT_OK && data!=null){
            authorFilter=data.getStringExtra("selected_author");if(authorFilter==null)authorFilter="";
            String chosen=data.getStringExtra("selected_category");
            if(chosen!=null && !chosen.isEmpty())category=chosen;
            if(search!=null)search.setText("");query="";filter();
            if(counter!=null)counter.requestFocus();
        }
    }

    JSONArray load(String p){try(InputStream original=getAssets().open(p);InputStream in=p.endsWith(".dat")?new java.util.zip.GZIPInputStream(original):original){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[65536];int n;while((n=in.read(b))>0)out.write(b,0,n);return new JSONArray(out.toString("UTF-8"));}catch(Exception e){return new JSONArray();}}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(DEEP);getWindow().setNavigationBarColor(DEEP);prefs=getSharedPreferences("dama_books",0);favorites=new HashSet<>(prefs.getStringSet("saved",new HashSet<>())); LinkedHashMap<String,Book> uniq=new LinkedHashMap<>(); for(String f:new String[]{"library_catalog.dat","ebook_catalog_updates.json"}){JSONArray x=f.equals("ebook_catalog_updates.json")?EbookSync.readSaved(this):load(f);for(int i=0;i<x.length();i++){JSONObject j=x.optJSONObject(i);if(j==null)continue;Book v=new Book(j);if(v.url.startsWith("http"))uniq.putIfAbsent(v.url,v);}}books.addAll(uniq.values());draw();}
    void syncBooksNow(){
        if(syncingBooks){msg("EBook Sync လုပ်နေဆဲပါ");return;}
        syncingBooks=true;ebookSyncDone=0;ebookSyncTotal=1;
        if(ebookSyncProgress!=null){ebookSyncProgress.setMax(1);ebookSyncProgress.setProgress(0);}
        if(ebookSyncLabel!=null)ebookSyncLabel.setText("EBook Publisher Pages စစ်နေပါသည် …");
        HashSet<String> currentUrls=new HashSet<>();for(Book book:books)currentUrls.add(book.url);
        EbookSync.run(this,currentUrls,new EbookSync.Listener(){
          public void onProgress(int checked,int total,int added,int errors,String source){
            ebookSyncDone=checked;ebookSyncTotal=Math.max(1,total);
            if(ebookSyncProgress!=null){ebookSyncProgress.setMax(ebookSyncTotal);ebookSyncProgress.setProgress(checked);}
            if(ebookSyncLabel!=null)ebookSyncLabel.setText(
              "EBOOK SYNC "+checked+"/"+total+" • "+(100*checked/Math.max(1,total))+"%  •  +"+added+"  • Errors "+errors);
          }
          public void onDone(int added,int checked,int errors,String diagnostic){
            syncingBooks=false;
            int actuallyAdded=0;
            HashSet<String> urls=new HashSet<>();for(Book book:books)urls.add(book.url);
            JSONArray saved=EbookSync.readSaved(LibraryActivity.this);
            for(int i=0;i<saved.length();i++){
              JSONObject o=saved.optJSONObject(i);if(o==null)continue;
              Book b=new Book(o);
              if(b.url.startsWith("https://")&&urls.add(b.url)){books.add(b);actuallyAdded++;}
            }
            String summary="EBOOK Sync: "+checked+" source pages checked • +"+actuallyAdded+" links • "+errors+" errors";
            prefs.edit().putString("ebook_sync_summary",summary).putLong("ebook_sync_last_at",System.currentTimeMillis()).apply();
            final int count=actuallyAdded;
            if(!isFinishing()&&!isDestroyed()){
              draw();
              new AlertDialog.Builder(LibraryActivity.this).setTitle("DAMA LIBRARY — EBook Sync Result")
                .setMessage(summary+"\n\n"+diagnostic+
                "\n\nNote: Limited publisher source pages, not a complete website crawl. Links are not individually verified.")
                .setPositiveButton("OK",null)
                .setNeutralButton("Source Website",(d,w)->open("https://www.dhammadownload.com/eBook-Myanmar.htm")).show();
            }
          }
        });
    }
    void addPremiumStat(LinearLayout row,String value,String caption){
        LinearLayout tile=vert();tile.setGravity(Gravity.CENTER);tile.setPadding(dp(5),dp(9),dp(5),dp(9));
        tile.setBackground(shape(0xff174238,14,0xff645f45));
        TextView count=t(value,19,GOLD,true);count.setGravity(Gravity.CENTER);tile.addView(count);
        TextView name=t(caption,9,MUTED,false);name.setGravity(Gravity.CENTER);tile.addView(name);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(75),1);
        p.setMargins(dp(3),dp(10),dp(3),0);row.addView(tile,p);
    }
    void addPremiumShortcut(LinearLayout row,String glyph,String label,String sub,Runnable click){
        LinearLayout tile=vert();tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(4),dp(9),dp(4),dp(6));
        tile.setBackground(shape(0xff183f36,15,0xff78704f));
        TextView ico=t(glyph,25,GOLD,true);ico.setGravity(Gravity.CENTER);tile.addView(ico);
        TextView name=t(label,12,IVORY,true);name.setGravity(Gravity.CENTER);tile.addView(name);
        TextView desc=t(sub,9,MUTED,false);desc.setGravity(Gravity.CENTER);tile.addView(desc);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(106),1);
        p.setMargins(dp(3),0,dp(3),0);row.addView(tile,p);
        tile.setOnClickListener(v->click.run());
    }

    void showSayadawProfile(int monkIndex){
        if(monkIndex<0||monkIndex>=SayadawProfilePopup.KEYS.length)return;
        int bookCount=0;
        String authorKey=SayadawProfilePopup.KEYS[monkIndex];
        for(Book b:books)if(b.author.contains(authorKey))bookCount++;
        final int selected=monkIndex;
        SayadawProfilePopup.open(this,selected,bookCount,()->showSayadawBooks(selected));
    }
    int bookListScrollTop(){
        if(counter==null||page==null)return 0;
        android.graphics.Rect r=new android.graphics.Rect();
        counter.getDrawingRect(r);
        page.offsetDescendantRectToMyCoords(counter,r);
        return Math.max(0,r.top-dp(15));
    }
    void showSayadawBooks(int monkIndex){
        // Match BOOK AUTHOR, not merely a keyword occurring in an unrelated title.
        String[] keys={"မိုးကုတ်","မဟာစည်","နန္ဒမာလာ"};
        String[] names={"မိုးကုတ်ဆရာတော်","မဟာစည်ဆရာတော်","နန္ဒမာလာဘိဝံသဆရာတော်"};
        if(monkIndex<0||monkIndex>=keys.length)return;
        sayadawKey=keys[monkIndex];
        authorFilter="";category="အားလုံး";fileFilter="ALL";statusFilter="ALL";
        favoritesOnly=false;offlineOnly=false;query="";
        if(search!=null)search.setText("");
        filter();
        if(libraryScroll!=null&&counter!=null)libraryScroll.post(()->libraryScroll.smoothScrollTo(0,
            Math.max(0,bookListScrollTop())));
        if(visible.isEmpty()){
            msg("ဤဆရာတော်၏ စာအုပ်ကို လက်ရှိ Catalog တွင် မတွေ့ရသေးပါ");
        }else{msg(names[monkIndex]+" — စာအုပ် "+visible.size()+" အုပ်");}
    }
    void donatePopup(){
        LinearLayout panel=vert();panel.setPadding(dp(19),dp(13),dp(19),dp(6));
        TextView heading=t("🙏  ဓမ္မအလှူ ပံ့ပိုးကူညီရန်",18,GOLD,true);
        panel.addView(heading);
        TextView msg=t("ဓမ္မစာပေ၊ တရားတော်အသံနှင့် ဗုဒ္ဓဘာသာပညာများကို အများပြည်သူ လေ့လာနိုင်ရေးအတွက် AAII.ASIA မှ ပံ့ပိုးကူညီထားပါသည်။\n\nဤစီမံကိန်းကို ဆက်လက်တိုးတက်အောင် ကူညီလိုပါက အောက်ပါ ဝက်ဘ်ဆိုက်များတွင် အသေးစိတ်ကြည့်ရှုနိုင်ပါသည်။\n\nအလှူငွေပေးရန် မဖြစ်မနေမလိုအပ်ပါ။",13,IVORY,false);
        msg.setPadding(0,dp(12),0,dp(14));panel.addView(msg);
        panel.addView(btn("🌐  DONATE BY AAII.ASIA",()->open("https://aaii.asia/")));
        panel.addView(btn("🌐  SOEMOE.AAII.ASIA",()->open("https://soemoe.aaii.asia/")));
        new AlertDialog.Builder(this).setView(panel)
            .setNegativeButton("ပိတ်မည်",null).show();
    }
    void featuredSayadaws(){
        TextView title=t("✦  FEATURED SAYADAWS",16,GOLD,true);
        title.setPadding(dp(2),dp(18),0,dp(9));page.addView(title);
        HorizontalScrollView horizontal=new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        LinearLayout gallery=row();
        int[] photos={R.drawable.mogok_sayadaw,R.drawable.mahasi_sayadaw,R.drawable.nandamala_sayadaw};
        String[] names={"မိုးကုတ်ဆရာတော်","မဟာစည်ဆရာတော်","နန္ဒမာလာဘိဝံသဆရာတော်"};
        for(int i=0;i<photos.length;i++){
            LinearLayout tile=vert();tile.setPadding(dp(9),dp(9),dp(9),dp(9));tile.setGravity(Gravity.CENTER);
            tile.setBackground(shape(0xff153d34,16,0xff977845));
            ImageView portrait=new ImageView(this);
            portrait.setImageResource(photos[i]);portrait.setScaleType(ImageView.ScaleType.CENTER_CROP);
            portrait.setBackground(shape(0xff294737,55,GOLD));portrait.setClipToOutline(true);
            tile.addView(portrait,new LinearLayout.LayoutParams(dp(84),dp(84)));
            TextView name=t(names[i],11,IVORY,true);name.setMaxLines(2);
            name.setGravity(Gravity.CENTER);tile.addView(name,new LinearLayout.LayoutParams(dp(102),dp(42)));
            LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(dp(124),dp(151));
            margin.setMargins(0,0,dp(9),0);gallery.addView(tile,margin);
            final int monkIndex=i;
            PremiumMedia.tap(tile,()->showSayadawProfile(monkIndex));
        }
        horizontal.addView(gallery);page.addView(horizontal);
        TextView note=t("ဆရာတော်ပုံများ — Wikimedia Commons မူရင်းများ (Credit ပါဝင်သည်)။",10,MUTED,false);
        note.setPadding(dp(3),dp(4),0,dp(1));page.addView(note);
    }
    void featuredBooks(){
        TextView title=t("▣  FEATURED BOOKS  •  COVER GALLERY",16,GOLD,true);
        title.setPadding(dp(3),dp(18),0,dp(10));page.addView(title);
        HorizontalScrollView horizontal=new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        LinearLayout rack=row();
        int limit=Math.min(books.size(),12);
        for(int i=0;i<limit;i++){
            Book book=books.get(i);
            LinearLayout box=vert();box.setGravity(Gravity.TOP);
            View art=PremiumMedia.decorativeBookCover(this,book.title,book.category,i,112,155);
            box.addView(art,new LinearLayout.LayoutParams(dp(112),dp(155)));
            TextView name=t(book.title,11,IVORY,true);name.setMaxLines(2);
            name.setGravity(Gravity.CENTER);box.addView(name,new LinearLayout.LayoutParams(dp(112),dp(42)));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(122),dp(202));
            p.setMargins(dp(2),0,dp(9),0);rack.addView(box,p);
            PremiumMedia.tap(box,()->details(book));
        }
        horizontal.addView(rack);page.addView(horizontal);
        TextView caution=t("Cover Gallery သည် စာအုပ်အမည်ဖြင့် ဖန်တီးထားသော ဒီဇိုင်းအဖုံးဖြစ်ပြီး မူရင်းစာအုပ် Cover မဟုတ်ပါ။ Offline PDF ရှိလျှင် စာမျက်နှာပထမပုံကို ပြနိုင်သည်။",10,MUTED,false);
        caution.setPadding(dp(3),dp(7),0,0);page.addView(caution);
    }
    void categoryBookCounts(){
        TextView heading=t("☸  CATEGORY LIBRARY",16,GOLD,true);heading.setPadding(dp(3),dp(17),0,dp(9));page.addView(heading);
        HashMap<String,Integer> counts=new HashMap<>();
        for(Book book:books)counts.put(book.category,counts.getOrDefault(book.category,0)+1);
        HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);
        LinearLayout cells=row();
        for(String cat:categories){
            final String select=cat;
            int n=cat.equals("အားလုံး")?books.size():counts.getOrDefault(cat,0);
            LinearLayout tile=vert();tile.setPadding(dp(12),dp(12),dp(12),dp(12));
            tile.setBackground(shape(cat.equals(category)?0xff305e43:0xff143d35,13,0xff8d7548));
            TextView name=t(cat,12,IVORY,true);name.setMaxLines(2);tile.addView(name);
            TextView total=t(String.format(java.util.Locale.US,"%,d books",n),11,GOLD,true);tile.addView(total);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(126),dp(72));p.setMargins(0,0,dp(8),0);cells.addView(tile,p);
            PremiumMedia.tap(tile,()->{category=select;filter();});
        }
        sc.addView(cells);page.addView(sc);
    }
    void draw(){main=vert();main.setBackgroundColor(BG);setContentView(main);
        if(android.os.Build.VERSION.SDK_INT>=35){main.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;
        });main.requestApplyInsets();}
        LinearLayout hdr=row();hdr.setPadding(dp(14),dp(14),dp(14),dp(10));hdr.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{DEEP,0xff17443a,DEEP}));TextView back=t("‹",32,GOLD,true);hdr.addView(back,new LinearLayout.LayoutParams(dp(43),dp(43)));back.setOnClickListener(v->finish());LinearLayout title=vert();title.addView(t("DAMA LIBRARY",22,GOLD,true));title.addView(t("ဓမ္မစာအုပ်များ • Offline Catalog",10,MUTED,false));hdr.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView readerShortcut=btn("▣  READER",()->startActivity(new Intent(this,ReaderShelfActivity.class)));
        readerShortcut.setBackground(shape(0xff285547,13,0xffbb9254));
        hdr.addView(readerShortcut);main.addView(hdr);
        ScrollView scroll=new ScrollView(this);libraryScroll=scroll;main.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));page=vert();page.setPadding(dp(12),dp(14),dp(12),dp(12));scroll.addView(page);
        android.widget.FrameLayout hero=new android.widget.FrameLayout(this);hero.setBackground(shape(CARD,20,GOLD));hero.setClipToOutline(true);ImageView cover=new ImageView(this);cover.setImageResource(R.drawable.dama_library_v12_hero);cover.setScaleType(ImageView.ScaleType.CENTER_CROP);hero.addView(cover,new android.widget.FrameLayout.LayoutParams(-1,dp(157)));LinearLayout words=vert();words.setPadding(dp(10),dp(10),dp(6),dp(6));words.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xe900201b,0x9000201b,0x00102018}));words.addView(t("DAMA LIBRARY",24,GOLD,true));words.addView(t("ဓမ္မစာအုပ်များ စုစည်းရာ",15,IVORY,true));words.addView(t("Read • Learn • Keep",10,IVORY,false));/* DAMA Library original branded art includes its own typography. */page.addView(hero,new LinearLayout.LayoutParams(-1,dp(185)));
        LinearLayout overview=row();
        java.util.HashSet<String> authorNames=new java.util.HashSet<>();
        for(Book book:books)if(!book.author.trim().isEmpty())authorNames.add(book.author);
        int offlineCount=0;
        File folder=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(folder==null)folder=getFilesDir();
        File[] stored=new File(folder,"DAMA-Library").listFiles(f->f.isFile()&&f.getName().endsWith(".pdf")&&f.length()>0);
        offlineCount=stored==null?0:stored.length;
        addPremiumStat(overview,String.valueOf(books.size()),"EBOOK LINKS");
        addPremiumStat(overview,String.valueOf(authorNames.size()),"AUTHORS / SETS");
        addPremiumStat(overview,String.valueOf(offlineCount),"OFFLINE PDFs");
        page.addView(overview);
        featuredSayadaws();
        TextView donateBtn=btn("♡  ဓမ္မအလှူ ပံ့ပိုးကူညီရန်  ·  DONATE BY AAII.ASIA",()->donatePopup());
        page.addView(donateBtn,new LinearLayout.LayoutParams(-1,dp(51)));
        featuredBooks();
        categoryBookCounts();
        TextView tagline=t("Discover · Read · Remember",15,GOLD,true);
        tagline.setPadding(dp(2),dp(17),0,dp(8));page.addView(tagline);
        LinearLayout quick=row();
        addPremiumShortcut(quick,"▣","EBOOK READER","Continue reading",()->startActivity(new Intent(this,ReaderShelfActivity.class)));
        addPremiumShortcut(quick,"☸","SAYADAWS","Author directory",()->openAuthorDirectory());
        addPremiumShortcut(quick,"♡","FAVORITES","Saved books",()->{favoritesOnly=!favoritesOnly;offlineOnly=false;filter();});
        page.addView(quick);
        LinearLayout syncPanel=vert();syncPanel.setPadding(dp(12),dp(13),dp(12),dp(13));
        syncPanel.setBackground(shape(0xff123b34,15,0xff746a4c));
        TextView syncTitle=t("↻  EBOOK LIST SYNC PROGRESS",13,GOLD,true);
        syncPanel.addView(syncTitle);
        ebookSyncLabel=t(syncingBooks?"EBook Sync "+ebookSyncDone+"/"+ebookSyncTotal:
            prefs.getString("ebook_sync_summary","Offline EBook Catalog "+books.size()+" links • Live sync not run"),11,MUTED,false);
        ebookSyncLabel.setPadding(0,dp(7),0,dp(9));syncPanel.addView(ebookSyncLabel);
        ebookSyncProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        ebookSyncProgress.setMax(Math.max(1,ebookSyncTotal));ebookSyncProgress.setProgress(syncingBooks?ebookSyncDone:0);
        ebookSyncProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
        ebookSyncProgress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff38574c));
        syncPanel.addView(ebookSyncProgress,new LinearLayout.LayoutParams(-1,dp(7)));
        TextView syncButton=btn("↻  Publisher မှ EBook အသစ်များစစ်ရန်",()->syncBooksNow());
        LinearLayout.LayoutParams syncBtnParams=new LinearLayout.LayoutParams(-1,-2);
        syncBtnParams.topMargin=dp(10);syncPanel.addView(syncButton,syncBtnParams);
        TextView syncNote=t("Publisher eBook စာမျက်နှာ အချို့ကိုသာ စစ်ပြီး Link Metadata အသစ်ကို Offline သိမ်းသည်။ PDF များ အလိုအလျောက် Download မလုပ်ပါ။",10,MUTED,false);
        syncNote.setPadding(0,dp(9),0,0);syncPanel.addView(syncNote);
        LinearLayout.LayoutParams syncWrap=new LinearLayout.LayoutParams(-1,-2);
        syncWrap.setMargins(0,dp(13),0,dp(7));page.addView(syncPanel,syncWrap);
        page.addView(t("စာအုပ်အမည်၊ စာရေးသူနှင့် လင့်ခ်များ Offline ပါဝင်သည်။ Link အလုပ်လုပ်မှုကို တစ်ခုချင်းစစ်ဆေးရန် လိုအပ်သည်။",10,MUTED,false));
        // Detailed author progress remains available in code; compact metrics lead this screen.
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(IVORY);search.setHintTextColor(0xffa7bcaf);search.setTextSize(14);search.setPadding(dp(13),dp(5),dp(13),dp(5));search.setBackground(shape(0xff183e37,15,0xffab8650));search.setHint("စာအုပ် / ဆရာတော် / PDF ရှာရန်…");search.setText(query);page.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){query=s.toString();filter();}public void afterTextChanged(Editable e){}});
        TextView authorTitle=t("✦  ဆရာတော် · စာရေးသူ · ကျမ်းစုစည်းမှု",16,GOLD,true);authorTitle.setPadding(0,dp(13),0,dp(3));page.addView(authorTitle);
        page.addView(t("အမည်နှိပ်ပြီး သက်ဆိုင်ရာစာအုပ်တွေကို တန်းကြည့်နိုင်ပါသည်။",10,MUTED,false));
        HorizontalScrollView authorscroll=new HorizontalScrollView(this);authorscroll.setHorizontalScrollBarEnabled(false);LinearLayout authorrow=row();
        LinkedHashMap<String,Integer> counts=new LinkedHashMap<>();for(Book book:books){if(!book.author.isEmpty())counts.put(book.author,counts.getOrDefault(book.author,0)+1);}
        ArrayList<String> persons=new ArrayList<>(counts.keySet());persons.sort((a,b)->Integer.compare(counts.get(b),counts.get(a)));
        int personCount=0;for(String name:persons){if(personCount++>=12)break;
            boolean collection=false;for(Book b:books){if(b.author.equals(name)){collection=b.isCollection();break;}}
            LinearLayout one=vert();one.setGravity(Gravity.CENTER);one.setPadding(dp(9),dp(11),dp(9),dp(9));one.setBackground(shape(CARD,13,0xff6b6648));
            TextView symbol=t(collection?"▤":"☸",24,GOLD,true);symbol.setGravity(Gravity.CENTER);symbol.setBackground(shape(0xff17443b,30,0xffb58a52));one.addView(symbol,new LinearLayout.LayoutParams(dp(52),dp(52)));
            TextView who=t(name,10,IVORY,true);who.setGravity(Gravity.CENTER);who.setMaxLines(2);one.addView(who,new LinearLayout.LayoutParams(dp(112),dp(42)));
            TextView count=t(counts.get(name)+" အုပ်",11,GOLD,true);count.setGravity(Gravity.CENTER);one.addView(count);
            LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-2,-2);pp.setMargins(0,0,dp(9),0);authorrow.addView(one,pp);
            one.setOnClickListener(v->{authorFilter=authorFilter.equals(name)?"":name;filter();});}
        authorscroll.addView(authorrow);page.addView(authorscroll);
        LinearLayout selectors=row();selectors.setPadding(0,dp(5),0,dp(6));
        selectors.addView(btn("☷ စာရေးသူ Profile ၆၄ ခု",()->openAuthorDirectory()),new LinearLayout.LayoutParams(0,-2,1));
        selectors.addView(btn("✕ Filter ဖယ်ရှား",()->{authorFilter="";sayadawKey="";category="အားလုံး";fileFilter="ALL";statusFilter="ALL";favoritesOnly=false;offlineOnly=false;query="";search.setText("");filter();}),new LinearLayout.LayoutParams(0,-2,1));
        page.addView(selectors);
        LinearLayout orderFilters=row();orderFilters.setPadding(0,0,0,dp(7));
        orderFilters.addView(btn("Format: "+(fileFilter.equals("ALL")?"PDF / DOC":fileFilter),()->chooseFormat()),new LinearLayout.LayoutParams(0,-2,1));
        orderFilters.addView(btn("Sort ⇅",()->chooseSort()),new LinearLayout.LayoutParams(0,-2,1));
        orderFilters.addView(btn("Link Status",()->chooseStatus()),new LinearLayout.LayoutParams(0,-2,1));
        page.addView(orderFilters);
        
        LinearLayout tabs=row();tabs.setPadding(0,dp(7),0,dp(7));tabs.addView(btn("♡ Favorites",()->{favoritesOnly=!favoritesOnly;if(favoritesOnly)offlineOnly=false;filter();}),new LinearLayout.LayoutParams(0,-2,1));tabs.addView(btn("↓ Downloads",()->{offlineOnly=!offlineOnly;if(offlineOnly)favoritesOnly=false;filter();}),new LinearLayout.LayoutParams(0,-2,1));page.addView(tabs);
        HorizontalScrollView scroller=new HorizontalScrollView(this);scroller.setHorizontalScrollBarEnabled(false);LinearLayout chips=row();for(String c:categories){final long nn=books.stream().filter(b->c.equals("အားလုံး")||b.category.equals(c)).count();TextView x=btn(c+" ("+nn+")",()->{category=c;filter();});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,dp(6),0);chips.addView(x,p);}scroller.addView(chips);page.addView(scroller);
        counter=t("",13,GOLD,true);counter.setPadding(dp(2),dp(13),0,dp(9));page.addView(counter);
        list=vert();page.addView(list);
        page.addView(btn("မူရင်း DhammaDownload စာကြည့်တိုက် ↗",()->open("https://www.dhammadownload.com/eBook-Myanmar.htm")));
        TextView rights=t("စာအုပ်လင့်ခ်များကို Offline သိမ်းထားသည်။ PDF ဖိုင်များကို ကိုယ်တိုင် Download လုပ်မှသာ Offline ဖတ်နိုင်သည်။ မူရင်းရေးသားသူများ၏ မူပိုင်ခွင့်ကို လေးစားပါ။",11,MUTED,false);rights.setPadding(0,dp(12),0,0);page.addView(rights);
        FooterNav.attach(this,main,"Library");
        filter();
    }
    void filter(){if(list==null)return;visible.clear();String q=query.toLowerCase(Locale.ROOT).trim();for(Book b:books){if(!category.equals("အားလုံး")&&!b.category.equals(category))continue;
          if(!fileFilter.equals("ALL")&&!(fileFilter.equals("PDF")?b.pdf():!b.pdf()))continue;
          if(!statusFilter.equals("ALL")&&!linkState(b).equals(statusFilter))continue;if(!authorFilter.isEmpty()&&!b.author.equals(authorFilter))continue;if(!sayadawKey.isEmpty()&&!b.author.contains(sayadawKey))continue;if(favoritesOnly&&!favorites.contains(b.id))continue;if(offlineOnly&&!local(b).isFile())continue;if(!q.isEmpty()&&!(b.title+" "+b.author+" "+b.category).toLowerCase(Locale.ROOT).contains(q))continue;visible.add(b);}
        if(sortMode.equals("TITLE"))Collections.sort(visible,(a,b)->a.title.compareToIgnoreCase(b.title));
        if(sortMode.equals("AUTHOR"))Collections.sort(visible,(a,b)->{int k=a.author.compareToIgnoreCase(b.author);return k!=0?k:a.title.compareToIgnoreCase(b.title);});
        if(sortMode.equals("CATEGORY"))Collections.sort(visible,(a,b)->{int k=a.category.compareTo(b.category);return k!=0?k:a.title.compareToIgnoreCase(b.title);});
        list.removeAllViews();rendered=0;counter.setText("စာအုပ် "+visible.size()+" အုပ် • "+category+(authorFilter.isEmpty()?"":" • "+authorFilter));showMore();}
    // Quick actions are visible on every card: tap Read to download-then-open in this app;
    // tap Save to download without leaving the Library screen.
    // Branded cover tiles are decorative; publisher-specific book jackets are not impersonated.
    void showMore(){
        int end=Math.min(visible.size(),rendered+28);
        for(int i=rendered;i<end;i++){
            final Book book=visible.get(i);
            LinearLayout card=vert();card.setPadding(dp(12),dp(12),dp(12),dp(12));
            GradientDrawable glass=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                     new int[]{0xff1a473f,0xff0e322f});
            glass.setCornerRadius(dp(17));glass.setStroke(dp(1),0xff54634b);
            card.setBackground(glass);card.setElevation(dp(2));
            LinearLayout line=row();
            FrameLayout coverFrame=new FrameLayout(this);
            View coverArt=PremiumMedia.decorativeBookCover(this,book.title,book.category,i,75,94);
            coverFrame.addView(coverArt,new FrameLayout.LayoutParams(dp(75),dp(94)));
            if(local(book).isFile()&&book.pdf()){
                ImageView preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                coverFrame.addView(preview,new FrameLayout.LayoutParams(dp(75),dp(94)));
                PremiumMedia.offlinePdfFirstPage(preview,local(book));
            }
            line.addView(coverFrame,new LinearLayout.LayoutParams(dp(75),dp(94)));
            LinearLayout info=vert();info.setPadding(dp(13),dp(4),dp(5),0);
            TextView category=t("✦ "+book.category,10,GOLD,true);category.setMaxLines(1);
            info.addView(category);
            TextView title=t(book.title,15,IVORY,true);title.setMaxLines(2);
            title.setPadding(0,dp(5),0,dp(4));info.addView(title);
            TextView author=t(book.author,11,MUTED,false);author.setMaxLines(1);
            info.addView(author);
            TextView status=t(book.pdf()?(local(book).exists()?"●  PDF  •  OFFLINE":"○  PDF  •  ONLINE"):"○  DOC  •  ONLINE",10,GOLD,false);
            info.addView(status);
            line.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            TextView detail=t("⋮",24,GOLD,true);detail.setGravity(Gravity.CENTER);
            line.addView(detail,new LinearLayout.LayoutParams(dp(26),dp(46)));
            PremiumMedia.tap(detail,()->details(book));PremiumMedia.tap(line,()->details(book));
            card.addView(line);
            LinearLayout actions=row();actions.setPadding(0,dp(10),0,0);
            TextView read=btn("▣  READ",()->{
                if(book.url.contains("drive.google.com/file/d/"))open(book.url);
                else if(book.pdf())openOrDownload(book,true);
                else open(book.url);
            });
            read.setBackground(shape(GOLD,11,0));read.setTextColor(DEEP);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(43),1);
            p.setMargins(0,0,dp(7),0);actions.addView(read,p);
            TextView download=btn(local(book).exists()?"✓ SAVED":"↓ SAVE",()->{
                if(book.url.contains("drive.google.com/file/d/"))open(book.url);
                else if(book.pdf())openOrDownload(book,false);
                else open(book.url);
            });
            actions.addView(download,new LinearLayout.LayoutParams(0,dp(43),1));
            TextView favorite=btn(favorites.contains(book.id)?"♥":"♡",()->{
                if(!favorites.add(book.id))favorites.remove(book.id);
                prefs.edit().putStringSet("saved",new HashSet<>(favorites)).apply();filter();
            });
            LinearLayout.LayoutParams favP=new LinearLayout.LayoutParams(dp(45),dp(43));
            favP.setMargins(dp(7),0,0,0);actions.addView(favorite,favP);
            card.addView(actions);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
            cp.setMargins(0,0,0,dp(11));list.addView(card,cp);
        }
        rendered=end;
        if(rendered<visible.size()){
            TextView more=btn("နောက်ထပ်စာအုပ် "+(visible.size()-rendered)+" အုပ်  ↓",()->{
                list.removeViewAt(list.getChildCount()-1);showMore();
            });
            more.setPadding(dp(14),dp(15),dp(14),dp(15));list.addView(more);
        }
    }
    String linkState(Book b){return prefs.getString("link_"+b.id,"UNCHECKED");}
    String stateLabel(Book b){String v=linkState(b);if(v.equals("OK"))return "✓ Verified on this device";if(v.equals("MISSING"))return "✕ Link missing";if(v.equals("INVALID"))return "! Not expected document";if(v.equals("BLOCKED"))return "? Access blocked";return "? Not verified";}
    void chooseAuthor(){openAuthorDirectory();}
    void chooseFormat(){String[] all={"PDF / DOC အားလုံး","PDF ၁,၈၄၉","Word DOC ၄၅"};new AlertDialog.Builder(this).setTitle("စာအုပ် Format").setSingleChoiceItems(all,fileFilter.equals("ALL")?0:fileFilter.equals("PDF")?1:2,(d,i)->{fileFilter=new String[]{"ALL","PDF","DOC"}[i];filter();d.dismiss();}).setNegativeButton("ပိတ်ရန်",null).show();}
    void chooseSort(){String[] a={"မူရင်းစာရင်း", "စာအုပ်အမည် (က-အ)","စာရေးသူ / ဆရာတော်အလိုက်","Category အလိုက်"};new AlertDialog.Builder(this).setTitle("Sort By").setSingleChoiceItems(a,sortMode.equals("DEFAULT")?0:sortMode.equals("TITLE")?1:sortMode.equals("AUTHOR")?2:3,(d,i)->{sortMode=new String[]{"DEFAULT","TITLE","AUTHOR","CATEGORY"}[i];filter();d.dismiss();}).setNegativeButton("ပိတ်ရန်",null).show();}
    void chooseStatus(){String[] a={"အားလုံး","ဖုန်းပေါ်စစ်ပြီး OK","Link မရှိ (404/410)","မစစ်ရသေး","Access Blocked","File Type မှား"};String[] vals={"ALL","OK","MISSING","UNCHECKED","BLOCKED","INVALID"};new AlertDialog.Builder(this).setTitle("Link Verification (per device)").setItems(a,(d,i)->{statusFilter=vals[i];filter();}).show();}
    // Only checks one item requested by the user. No bulk 1,894-URL probing.
    void checkLink(Book b){msg("Link စစ်ဆေးနေပါသည်…");new Thread(()->{
       String state="BLOCKED";String info="";HttpURLConnection c=null;
       try{
          URL u=new URL(b.url);if(!u.getProtocol().equalsIgnoreCase("https"))throw new IOException("HTTPS required");
          c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(true);c.setRequestProperty("Range","bytes=0-15");c.setRequestProperty("User-Agent","Mozilla/5.0 DAMA-Library/4.0");
          int code=c.getResponseCode();info="HTTP "+code;
          if(code==404||code==410)state="MISSING";
          else if(code==200||code==206){try(InputStream in=c.getInputStream()){
              byte[] head=new byte[16];int n=in.read(head);String start=new String(head,0,Math.max(0,n),StandardCharsets.ISO_8859_1);
              if(b.pdf())state=start.startsWith("%PDF-")?"OK":"INVALID";
              else state=(n>=8&&(head[0]&255)==0xd0&&(head[1]&255)==0xcf||(start.startsWith("PK")))?"OK":"INVALID";
          }}
       }catch(Exception ex){info=ex.getClass().getSimpleName()+": "+ex.getMessage();}finally{if(c!=null)c.disconnect();}
       final String s=state,desc=info;prefs.edit().putString("link_"+b.id,s).apply();runOnUiThread(()->{msg(stateLabel(b)+" · "+desc);filter();});
    }).start();}
    void details(Book b) {
        String fav=favorites.contains(b.id)?"♥ Favorites မှ ဖယ်ရှား":"♡ Favorites ထဲသိမ်း";
        String[] options={b.pdf()?"📖 Native APK ထဲ တန်းဖတ်မည်":"↗ Word DOC ဖွင့်ရန်",
            b.pdf()?"↓ PDF Download လုပ်မည်":"↗ DOC မူရင်းလင့်ခ်",
            local(b).exists()?"📚 Offline သိမ်းထားသော PDF ဖတ်ရန်":"📚 Offline PDF မရှိသေး",
            fav,"🔎 Link အလုပ်လုပ်မှုစစ်ရန်","↗ မူရင်း Website"};
        new AlertDialog.Builder(this).setTitle(b.title)
            .setMessage(b.author+"\n"+b.category+" · "+b.format+"\n"+b.status)
            .setItems(options,(d,i)->{
                if(i==0){if(b.pdf())openOrDownload(b,true);else open(b.url);}
                else if(i==1){if(b.pdf())openOrDownload(b,false);else open(b.url);}
                else if(i==2){if(local(b).exists())read(b);else msg("PDF မသိမ်းရသေးပါ");}
                else if(i==3){if(!favorites.add(b.id))favorites.remove(b.id);
                    prefs.edit().putStringSet("saved",new HashSet<>(favorites)).apply();filter();}
                else if(i==4)checkLink(b);
                else open(b.listUrl.isEmpty()?b.url:b.listUrl);
            }).show();
    }
    void open(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception ex){msg("Link ဖွင့်မရပါ");}}
    void read(Book b){
        File pdf=local(b);
        if(!pdf.isFile()){msg("PDF ကို Download လုပ်ရန် လိုအပ်ပါသည်");return;}
        Intent i=new Intent(this,PdfReaderActivity.class);
        i.putExtra("path",pdf.getAbsolutePath());i.putExtra("title",b.title);startActivity(i);
    }
    File local(Book b){
        File folder=getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if(folder==null)folder=getFilesDir();
        File dir=new File(folder,"DAMA-Library");if(!dir.exists())dir.mkdirs();
        return new File(dir,b.id+".pdf");
    }
    private volatile boolean downloading=false;
    void openOrDownload(Book b,boolean openAfter) {
        if(!b.pdf()){open(b.url);return;}
        File dst=local(b);
        if(dst.isFile()){
            if(openAfter)read(b);else msg("ဤ PDF ကို Offline သိမ်းထားပြီးပါပြီ");
            return;
        }
        if(downloading){msg("အခြားစာအုပ်တစ်အုပ် Download လုပ်နေပါသည်");return;}
        // Downloads are an explicit user action; only the selected title is fetched.
        final java.util.concurrent.atomic.AtomicBoolean cancelled=new java.util.concurrent.atomic.AtomicBoolean(false);
        LinearLayout panel=vert();panel.setPadding(dp(19),dp(11),dp(19),dp(10));
        TextView label=t(b.title,13,DEEP,true);panel.addView(label);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);progress.setIndeterminate(true);panel.addView(progress);
        TextView desc=t("မူရင်း Website မှ PDF ရယူနေသည်…",12,DEEP,false);desc.setPadding(0,dp(10),0,0);panel.addView(desc);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle(openAfter?"PDF ရယူပြီး Native Reader ဖြင့်ဖတ်ရန်":"PDF Offline Download")
            .setView(panel).setNegativeButton("Cancel",(d,w)->cancelled.set(true)).create();
        dialog.setOnCancelListener(d->cancelled.set(true));
        dialog.show();downloading=true;
        new Thread(()->{
            File part=new File(dst.getAbsolutePath()+".part");String error=null;
            HttpURLConnection connection=null;
            try {
                URL url=new URL(b.url);
                if(!"https".equalsIgnoreCase(url.getProtocol()))throw new IOException("HTTPS လင့်ခ် လိုအပ်သည်");
                connection=(HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(15000);connection.setReadTimeout(35000);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("User-Agent","Mozilla/5.0 (Android; DAMA-Library/5.0)");
                int response=connection.getResponseCode();
                if(response!=200)throw new IOException("Website HTTP "+response);
                if(!"https".equalsIgnoreCase(connection.getURL().getProtocol()))throw new IOException("Unsafe redirect");
                long total=connection.getContentLengthLong();
                final long maxSize=120L*1024*1024;
                if(total>maxSize)throw new IOException("PDF 120MB ထက်ကြီးသည်");
                long count=0,lastUpdate=0;int priorPercent=-1;
                try(InputStream in=connection.getInputStream();FileOutputStream out=new FileOutputStream(part)) {
                    byte[] bytes=new byte[32768];int n;
                    while((n=in.read(bytes))!=-1){
                        if(cancelled.get())throw new IOException("Download cancelled");
                        count+=n;
                        if(count>maxSize)throw new IOException("PDF size exceeds 120MB");
                        out.write(bytes,0,n);
                        long now=android.os.SystemClock.elapsedRealtime();
                        int percent=total>0?(int)Math.min(100,(count*100)/total):-1;
                        if(now-lastUpdate>=350 || (percent>=0&&percent!=priorPercent&&percent%5==0)){
                            final int pct=percent;final long bytesNow=count;
                            runOnUiThread(()->{
                                if(isFinishing()||isDestroyed())return;
                                if(pct>=0){progress.setIndeterminate(false);progress.setProgress(pct);
                                    desc.setText(pct+"% · "+(bytesNow/1024)+" KB Downloaded");}
                                else desc.setText((bytesNow/1024)+" KB Downloaded");
                            });
                            lastUpdate=now;priorPercent=percent;
                        }
                    }
                    out.getFD().sync();
                }
                if(count<8)throw new IOException("Empty PDF response");
                try(FileInputStream fin=new FileInputStream(part)){
                    byte[] magic=new byte[5];
                    if(fin.read(magic)!=5 || !java.util.Arrays.equals(magic,"%PDF-".getBytes(StandardCharsets.US_ASCII)))
                        throw new IOException("Website returned HTML, not a PDF");
                }
                if(cancelled.get())throw new IOException("Download cancelled");
                if(!part.renameTo(dst))throw new IOException("Cannot save PDF");
            } catch(Exception e){error=e.getMessage();part.delete();}
            finally{if(connection!=null)connection.disconnect();}
            final String failure=error;
            runOnUiThread(()->{
                downloading=false;
                try{dialog.dismiss();}catch(Exception ignored){}
                if(failure==null){msg("PDF Download အောင်မြင်ပါပြီ — Offline ဖတ်နိုင်ပါသည်");filter();if(openAfter)read(b);}
                else if(!cancelled.get()){
                    new AlertDialog.Builder(this).setTitle("PDF မရရှိပါ")
                        .setMessage(failure+"\n\nမူရင်း Website မှ ဖိုင်လင့်ခ် ပြောင်းထားနိုင်သည်။")
                        .setPositiveButton("မူရင်း Website",(d,w)->open(b.listUrl.isEmpty()?b.url:b.listUrl))
                        .setNegativeButton("ပိတ်ရန်",null).show();
                }
            });
        },"DAMA-Book-Download").start();
    }
}