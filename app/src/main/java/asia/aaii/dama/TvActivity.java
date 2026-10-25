package asia.aaii.dama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/** Native DAMA TV browser with the official YouTube IFrame Player embedded in WebView.
 * Metadata is bundled offline. Videos always stream from YouTube under its own controls and policies.
 * No media stream extraction, credential injection or ad bypass.
 */
public class TvActivity extends Activity {
    private static final int BG=0xff071f20, DARK=0xff0a302d, CARD=0xff18443a;
    private static final int GOLD=0xffeec778, IVORY=0xfffff3da, MUTED=0xffc5c8b5;
    private static final String EMBED_ORIGIN="https://aaii.asia/"; // nonopaque, HTTPS document origin provides Referer to YouTube iframe
    private final ArrayList<Video> videos=new ArrayList<>(),filtered=new ArrayList<>();
    private final ArrayList<String> monks=new ArrayList<>(),categories=new ArrayList<>(),sources=new ArrayList<>();
    private final LinkedHashSet<String> favorites=new LinkedHashSet<>();
    private final LinkedList<String> recent=new LinkedList<>();
    private SharedPreferences prefs;
    private LinearLayout root,topRow,filterPanel;
    private ListView videoList;
    private TextView resultCount,tabAll,tabFav,tabRecent,tabSources;
    private Spinner monkSelect,categorySelect,sourceSelect;
    private EditText queryInput;
    private int monkIndex=0,categoryIndex=0,sourceIndex=0;
    private String section="All",query="";
    private Video selected;
    private WebView web;
    private FrameLayout fullscreen;
    private View customVideoView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    static final class Video {
        String id,title,sayadaw,category,source,sourceUrl;
        int duration;
        Video(JSONObject o){id=o.optString("id"); title=o.optString("title"); sayadaw=o.optString("sayadaw");
            category=o.optString("category");source=o.optString("source");sourceUrl=o.optString("source_url");
            duration=o.optInt("duration",0);}
    }
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    LinearLayout col(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    LinearLayout.LayoutParams wh(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    GradientDrawable bg(int fill,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    TextView text(String value,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextColor(color);v.setTextSize(size);v.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    TextView button(String label,Runnable click){TextView b=text(label,13,GOLD,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(10),dp(10),dp(10),dp(10));b.setBackground(bg(DARK,11,0xff9b7c43));b.setOnClickListener(v->click.run());return b;}
    void gap(LinearLayout parent,int n){View v=new View(this);parent.addView(v,wh(1,n));}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("dama_tv_v6",MODE_PRIVATE);favorites.addAll(prefs.getStringSet("favorites",Collections.emptySet()));
        try{JSONArray a=new JSONArray(prefs.getString("recent","[]"));for(int i=0;i<a.length();i++)recent.add(a.optString(i));}catch(Exception ignored){}
        loadCatalog();showBrowse();}
    private void loadCatalog(){
        try(InputStream gzip=getAssets().open("tv_catalog.json");ByteArrayOutputStream output=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int n;while((n=gzip.read(buffer))!=-1)output.write(buffer,0,n);
            JSONArray list=new JSONArray(output.toString("UTF-8"));
            LinkedHashSet<String> people=new LinkedHashSet<>(),types=new LinkedHashSet<>(),channels=new LinkedHashSet<>(),ids=new LinkedHashSet<>();
            for(int i=0;i<list.length();i++){JSONObject o=list.optJSONObject(i);if(o==null)continue;Video v=new Video(o);
                if(!v.id.matches("[A-Za-z0-9_-]{11}")||!ids.add(v.id))continue;
                videos.add(v);people.add(v.sayadaw);types.add(v.category);channels.add(v.source);}
            monks.add("ဆရာတော်အားလုံး");monks.addAll(people);
            categories.add("အမျိုးအစားအားလုံး");categories.addAll(types);
            sources.add("Channel / Playlist အားလုံး");sources.addAll(channels);
        }catch(Exception e){new AlertDialog.Builder(this).setTitle("DAMA TV Catalog Error").setMessage(e.getMessage()).setPositiveButton("OK",null).show();}
        if(monks.isEmpty())monks.add("ဆရာတော်အားလုံး");if(categories.isEmpty())categories.add("အမျိုးအစားအားလုံး");if(sources.isEmpty())sources.add("Channel / Playlist အားလုံး");
    }
    private void reset(String title){root=col();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=row();header.setPadding(dp(14),dp(9),dp(14),dp(9));header.setBackgroundColor(DARK);
        TextView back=button("‹",()->{if(selected!=null)exitPlayer();else finish();});header.addView(back,wh(42,44));
        LinearLayout heading=col();heading.setPadding(dp(12),0,0,0);heading.addView(text(title,20,GOLD,true));heading.addView(text("ဓမ္မရုပ်သံ • In-App YouTube Player",10,MUTED,false));header.addView(heading,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView home=button("DAMA",()->{releasePlayer();finish();});header.addView(home);root.addView(header);
    }
    private void showBrowse(){selected=null;releasePlayer();reset("DAMA TV");
        ScrollView headerScroll=new ScrollView(this);headerScroll.setFillViewport(false);headerScroll.setVerticalScrollBarEnabled(false);
        filterPanel=col();filterPanel.setPadding(dp(14),dp(12),dp(14),dp(3));headerScroll.addView(filterPanel);root.addView(headerScroll,wh(-1,-2));
        LinearLayout brand=row();brand.setPadding(dp(12),dp(10),dp(12),dp(10));brand.setBackground(bg(CARD,14,0xffa6894a));
        ImageView image=new ImageView(this);image.setImageResource(R.drawable.tv_banner);image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(bg(DARK,10,0));image.setClipToOutline(true);brand.addView(image,wh(100,68));
        LinearLayout words=col();words.setPadding(dp(12),0,0,0);words.addView(text("YouTube တရားတော်",16,IVORY,true));
        words.addView(text(videos.size()+" video links • Offline Catalog",11,GOLD,false));
        words.addView(text("အင်တာနက်ဖြင့် APK ထဲမှာ တိုက်ရိုက်ကြည့်ရန်",10,MUTED,false));
        brand.addView(words,new LinearLayout.LayoutParams(0,-2,1));filterPanel.addView(brand);gap(filterPanel,9);
        queryInput=new EditText(this);queryInput.setSingleLine(true);queryInput.setTextColor(IVORY);queryInput.setHintTextColor(MUTED);queryInput.setTextSize(13);
        queryInput.setHint("⌕ Video ခေါင်းစဉ် / ဆရာတော် ရှာရန်");queryInput.setPadding(dp(14),dp(10),dp(14),dp(10));queryInput.setBackground(bg(DARK,12,0xff796a4c));queryInput.setText(query);
        filterPanel.addView(queryInput,wh(-1,48));gap(filterPanel,8);
        LinearLayout tabs=row();String[] names={"All","Favorites","Recent","Playlists"};for(String name:names){TextView b=text(name,11,name.equals(section)?GOLD:MUTED,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(2),dp(10),dp(2),dp(10));
            b.setBackground(bg(name.equals(section)?CARD:DARK,10,name.equals(section)?GOLD:0));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(41),1);p.setMargins(dp(2),0,dp(2),0);tabs.addView(b,p);b.setOnClickListener(v->{section=name;showBrowse();});}
        filterPanel.addView(tabs);gap(filterPanel,8);
        monkSelect=spinner(monks,monkIndex,index->{monkIndex=index;filterList();});filterPanel.addView(monkSelect,wh(-1,42));gap(filterPanel,5);
        categorySelect=spinner(categories,categoryIndex,index->{categoryIndex=index;filterList();});filterPanel.addView(categorySelect,wh(-1,42));gap(filterPanel,5);
        sourceSelect=spinner(sources,sourceIndex,index->{sourceIndex=index;filterList();});filterPanel.addView(sourceSelect,wh(-1,42));gap(filterPanel,7);
        resultCount=text("Video စာရင်းပြင်ဆင်နေသည်…",12,GOLD,false);filterPanel.addView(resultCount);gap(filterPanel,4);
        videoList=new ListView(this);videoList.setBackgroundColor(BG);videoList.setDividerHeight(dp(4));videoList.setCacheColorHint(BG);
        root.addView(videoList,new LinearLayout.LayoutParams(-1,0,1));
        videoList.setOnItemClickListener((p,v,index,id)->{if(section.equals("Playlists")){sourceIndex=index+1;section="All";showBrowse();}else if(index<filtered.size())openVideo(filtered.get(index));});
        queryInput.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();filterList();}
            public void afterTextChanged(Editable e){} });
        filterList();
    }
    interface Chosen{void selected(int index);}
    Spinner spinner(List<String> entries,int initial,Chosen chosen){Spinner spinner=new Spinner(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,entries){
            @Override public View getView(int position,View convert,ViewGroup parent){TextView t=(TextView)super.getView(position,convert,parent);
                t.setTextColor(GOLD);t.setTextSize(12);t.setPadding(dp(10),dp(7),dp(4),dp(7));return t;}};
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);spinner.setAdapter(adapter);spinner.setBackground(bg(DARK,11,0xff665a44));spinner.setSelection(Math.max(0,Math.min(initial,entries.size()-1)));
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View view,int idx,long id){chosen.selected(idx);}
            public void onNothingSelected(android.widget.AdapterView<?> p){}});return spinner;
    }
    void filterList(){if(videoList==null||selected!=null)return;
        if(section.equals("Playlists")){
            List<String> choices=new ArrayList<>();for(int i=1;i<sources.size();i++)choices.add(sources.get(i));
            resultCount.setText(choices.size()+" Channel / Playlist Sources");
            videoList.setAdapter(new BaseAdapter(){public int getCount(){return choices.size();}public Object getItem(int p){return choices.get(p);}public long getItemId(int p){return p;}
                public View getView(int p,View old,ViewGroup parent){TextView item=text("▶  "+choices.get(p),14,IVORY,true);item.setPadding(dp(20),dp(13),dp(10),dp(13));item.setBackground(bg(CARD,12,0));return item;}});return;
        }
        filtered.clear();String needle=query.trim().toLowerCase(Locale.ROOT);
        for(Video v:videos){if(monkIndex>0&&!v.sayadaw.equals(monks.get(monkIndex)))continue;
            if(categoryIndex>0&&!v.category.equals(categories.get(categoryIndex)))continue;
            if(sourceIndex>0&&!v.source.equals(sources.get(sourceIndex)))continue;
            if(section.equals("Favorites")&&!favorites.contains(v.id))continue;
            if(section.equals("Recent")&&!recent.contains(v.id))continue;
            if(!needle.isEmpty()&&!(v.title+" "+v.sayadaw+" "+v.category+" "+v.source).toLowerCase(Locale.ROOT).contains(needle))continue;
            filtered.add(v);
        }
        if(section.equals("Recent"))filtered.sort(Comparator.comparingInt(v->{int n=recent.indexOf(v.id);return n<0?1000:n;}));
        resultCount.setText("Videos: "+filtered.size()+" / "+videos.size()+" • Tap to play inside DAMA TV");
        videoList.setAdapter(new BaseAdapter(){public int getCount(){return filtered.size();}public Object getItem(int p){return filtered.get(p);}public long getItemId(int p){return p;}
            public View getView(int index,View old,ViewGroup parent){Video v=filtered.get(index);LinearLayout item=row();item.setPadding(dp(8),dp(10),dp(7),dp(10));item.setBackground(bg(CARD,13,0));
                LinearLayout art=col();art.setGravity(Gravity.CENTER);art.setBackground(bg(DARK,9,0xff94763b));TextView play=text("▶",26,GOLD,true);play.setGravity(Gravity.CENTER);art.addView(play);item.addView(art,wh(73,68));
                LinearLayout info=col();info.setPadding(dp(10),dp(2),dp(2),0);TextView title=text(v.title,13,IVORY,true);title.setMaxLines(2);info.addView(title);
                TextView monk=text(v.sayadaw,10,GOLD,false);monk.setMaxLines(1);info.addView(monk);
                TextView detail=text(v.category+" • "+duration(v.duration),10,MUTED,false);info.addView(detail);item.addView(info,new LinearLayout.LayoutParams(0,-2,1));
                TextView mark=text(favorites.contains(v.id)?"♥":"▶",21,GOLD,true);mark.setGravity(Gravity.CENTER);item.addView(mark,wh(31,57));return item;
            }});
    }
    static String duration(int seconds){return seconds>0?String.format(Locale.US,"%02d:%02d",seconds/60,seconds%60):"—";}
    private void openVideo(Video v){selected=v;recent.remove(v.id);recent.addFirst(v.id);while(recent.size()>75)recent.removeLast();prefs.edit().putString("recent",new JSONArray(recent).toString()).apply();
        reset("NOW PLAYING");
        ScrollView sc=new ScrollView(this);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));LinearLayout content=col();content.setPadding(dp(12),dp(12),dp(12),dp(14));sc.addView(content);
        TextView title=text(v.title,19,IVORY,true);content.addView(title);gap(content,3);
        content.addView(text(v.sayadaw+"  ·  "+v.category,12,GOLD,false));gap(content,12);
        // Keep the YouTube player fully visible; minimum 200px square under official embedding policy.
        FrameLayout playerFrame=new FrameLayout(this);playerFrame.setBackgroundColor(Color.BLACK);content.addView(playerFrame,wh(-1,250));
        web=new WebView(this);web.setBackgroundColor(Color.BLACK);web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);web.getSettings().setMediaPlaybackRequiresUserGesture(true);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);
        web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
            if(!request.isForMainFrame())return false;
            Uri uri=request.getUrl();String host=uri.getHost();
            if(host!=null&&host.equalsIgnoreCase("aaii.asia"))return false;
            launchExternal(uri.toString());return true;}});
        web.setWebChromeClient(new WebChromeClient(){@Override public void onShowCustomView(View view,CustomViewCallback callback){
            if(customVideoView!=null){callback.onCustomViewHidden();return;}
            customVideoView=view;customViewCallback=callback;fullscreen=new FrameLayout(TvActivity.this);fullscreen.setBackgroundColor(Color.BLACK);
            fullscreen.addView(view,new FrameLayout.LayoutParams(-1,-1));addContentView(fullscreen,new ViewGroup.LayoutParams(-1,-1));
            getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
        }
        @Override public void onHideCustomView(){closeFullscreen();}});
        playerFrame.addView(web,new FrameLayout.LayoutParams(-1,-1));
        // A proper enclosing HTTPS document makes YouTube receive an HTTP Referer; direct /embed loading can trigger error 153.
        String src="https://www.youtube-nocookie.com/embed/"+v.id+"?playsinline=1&controls=1&rel=0";
        String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1.0'>"+
            "<meta name='referrer' content='strict-origin-when-cross-origin'><style>html,body{margin:0;width:100%;height:100%;background:#000;}"+
            "iframe{border:0;width:100%;height:100%;}</style></head><body><iframe title='YouTube Player' src='"+src+
            "' allow='accelerometer;autoplay;clipboard-write;encrypted-media;gyroscope;picture-in-picture;web-share' allowfullscreen referrerpolicy='strict-origin-when-cross-origin'></iframe></body></html>";
        web.loadDataWithBaseURL(EMBED_ORIGIN,html,"text/html","UTF-8",null);
        gap(content,10);
        TextView note=text("YouTube Player ဖြင့် APK အတွင်း တိုက်ရိုက်ကြည့်ပါ။ Embed ပိတ်ထားသော / ဖျက်ထားသော Video များတွင် မဖွင့်နိုင်ပါ။",11,MUTED,false);
        content.addView(note);gap(content,12);
        LinearLayout actions=row();actions.addView(button(favorites.contains(v.id)?"♥ Favorite":"♡ Favorite",()->{toggleFavorite(v);openVideo(v);}));
        actions.addView(button("YouTube တွင်ဖွင့်ရန် ↗",()->launchExternal("https://www.youtube.com/watch?v="+v.id)));
        content.addView(actions);gap(content,9);
        content.addView(button("စာရင်းသို့ပြန်ရန်",()->exitPlayer()));gap(content,15);
        content.addView(text("Channel / Playlist: "+v.source,12,IVORY,true));gap(content,8);
        content.addView(button("မူရင်း Channel / Playlist",()->launchExternal(v.sourceUrl)));
        gap(content,12);content.addView(text("ဗီဒီယိုဒေတာ Offline သိမ်းထားပြီး Video ကို YouTube မှ Online ဖွင့်ပါသည်။",11,MUTED,false));
    }
    private void toggleFavorite(Video v){if(!favorites.add(v.id))favorites.remove(v.id);prefs.edit().putStringSet("favorites",new LinkedHashSet<>(favorites)).apply();}
    private void launchExternal(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
        catch(ActivityNotFoundException e){Toast.makeText(this,"YouTube / Browser မရှိပါ",Toast.LENGTH_LONG).show();}}
    private void closeFullscreen(){if(customVideoView!=null){if(fullscreen!=null){fullscreen.removeView(customVideoView);((ViewGroup)fullscreen.getParent()).removeView(fullscreen);}
            customVideoView=null;fullscreen=null;getWindow().getDecorView().setSystemUiVisibility(0);
            if(customViewCallback!=null){customViewCallback.onCustomViewHidden();customViewCallback=null;}}}
    private void releasePlayer(){closeFullscreen();if(web!=null){web.stopLoading();web.loadUrl("about:blank");web.onPause();web.destroy();web=null;}}
    private void exitPlayer(){selected=null;showBrowse();}
    @Override public void onBackPressed(){if(customVideoView!=null){closeFullscreen();return;}if(selected!=null){exitPlayer();return;}super.onBackPressed();}
    @Override protected void onPause(){if(web!=null)web.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
    @Override protected void onDestroy(){releasePlayer();super.onDestroy();}
}