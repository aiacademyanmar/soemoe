package asia.aaii.buddhapsychology.preview;
import android.app.*; import android.os.*; import android.content.*; import android.graphics.*;
import android.graphics.pdf.PdfRenderer; import android.media.MediaPlayer; import android.view.*; import android.widget.*;
import android.content.res.AssetFileDescriptor; import java.io.*; import org.json.*;
public class MainActivity extends Activity {
  final int teal=0xff134649, gold=0xffb78c43, bg=0xfffaf8f2; LinearLayout panel;
  MediaPlayer player; PdfRenderer pdf; ParcelFileDescriptor pdfFile; ImageView pdfView; TextView pdfLabel; int page=0;
  JSONObject catalog;
  @Override public void onCreate(Bundle b){super.onCreate(b);home();}
  TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(teal);t.setPadding(13,10,13,10);return t;}
  void header(String s){ TextView t=text(s,23);t.setTypeface(null,1);t.setTextColor(0xffe9c576);t.setBackgroundColor(teal);panel.addView(t); }
  LinearLayout col(){LinearLayout p=new LinearLayout(this);p.setOrientation(1);return p;}
  void screen(String title){
    closePdf();stopAudio();LinearLayout outer=col();outer.setBackgroundColor(bg);
    ScrollView scroll=new ScrollView(this);panel=col();panel.setPadding(14,12,14,20);scroll.addView(panel);
    outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout tabs=new LinearLayout(this);tabs.setBackgroundColor(teal);
    tab(tabs,"Home",()->home());tab(tabs,"Books",()->book());tab(tabs,"Audio",()->audio());tab(tabs,"Courses",()->tracks());
    outer.addView(tabs,new LinearLayout.LayoutParams(-1,60));setContentView(outer);header(title);
  }
  void tab(LinearLayout bar,String label,Runnable task){TextView t=text(label,13);t.setTextColor(0xfff5dd9c);t.setGravity(Gravity.CENTER);bar.addView(t,new LinearLayout.LayoutParams(0,-1,1));t.setOnClickListener(v->task.run());}
  void action(String label,Runnable run){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setOnClickListener(v->run.run());panel.addView(b);}
  void paragraph(String s){TextView t=text(s,16);t.setTextIsSelectable(true);panel.addView(t);}
  void image(int id,int height){ImageView i=new ImageView(this);i.setImageResource(id);i.setScaleType(ImageView.ScaleType.CENTER_CROP);panel.addView(i,new LinearLayout.LayoutParams(-1,height));}
  void home(){
    screen("Buddha Psychology · V5.0");
    image(R.drawable.buddha_hero,185);
    paragraph("ဗုဒ္ဓစိတ်ပညာ • Offline Learning — Rebuild Preview");
    action("စာအုပ် PDF ဖတ်ရန် · Offline PDF",()->book());
    action("မူရင်းစာအုပ်စာသား ၁၀ ပိုင်း",()->sections());
    action("တရားအသံ MP3 (၉ ဖိုင်)",()->audio());
    action("ခေါင်းစဉ် ၅၀၀ · Chapter Outline",()->tracks());
    action("Practice Quiz · နမူနာ",()->quiz());
    action("Certificate · Preview only",()->certificate());
    paragraph("ဤ App သည် မူရင်း V1.6.5 APK ကို ပြန်လည်တည်ဆောက်ထားသော Preview ဖြစ်ပြီး Original UI ကို Pixel-Perfect မဆိုလိုပါ။");
    // Single home advertisement slot intentionally empty until an approved HTTPS admin API is connected.
  }
  JSONObject data(){if(catalog!=null)return catalog;try{InputStream is=getAssets().open("psychology_courses_500.json");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=is.read(b))>0)out.write(b,0,n);is.close();catalog=new JSONObject(out.toString("UTF-8"));return catalog;}catch(Exception e){return new JSONObject();}}
  void tracks(){screen("Course Catalog · 500 Outlines");paragraph("Chapter Outlines / Teaching Prompts သာဖြစ်ပြီး Full Lessons ၅၀၀ မဟုတ်ပါ။");
    JSONArray list=data().optJSONArray("tracks");if(list==null){paragraph("Catalog unavailable");return;}
    for(int i=0;i<list.length();i++){JSONObject item=list.optJSONObject(i);if(item==null)continue; final int tid=item.optInt("id");
      action(item.optString("title_my")+" ("+item.optInt("from")+"–"+item.optInt("to")+")",()->chapters(tid));}}
  void chapters(int tid){screen("Track "+tid);JSONArray list=data().optJSONArray("chapters");if(list==null)return;
    for(int i=0;i<list.length();i++){JSONObject item=list.optJSONObject(i);if(item==null||item.optInt("track")!=tid)continue;
      String title=item.optInt("id")+" · "+item.optString("title_my");int index=i;action(title,()->chapter(index));}}
  void chapter(int idx){JSONArray list=data().optJSONArray("chapters");if(list==null)return;JSONObject item=list.optJSONObject(idx);if(item==null)return;
    screen("Chapter "+item.optInt("id"));paragraph(item.optString("title_my"));paragraph(item.optString("activity_my"));
    paragraph("Teaching Prompt (not a complete authored lesson)");paragraph(item.optString("prompt"));
    action("Copy Teaching Prompt",()->{android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("Teaching Prompt",item.optString("prompt")));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show();});
  }
  void sections(){screen("Original Book Text · 10 Sections");for(int i=0;i<10;i++){final int j=i;action("Section "+(j+1),()->section(j));}}
  void section(int j){screen("Book Text · Section "+(j+1));try{InputStream is=getAssets().open(String.format(java.util.Locale.US,"book_text/section_%02d.txt",j));ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=is.read(b))!=-1)out.write(b,0,n);is.close();paragraph(out.toString("UTF-8"));}catch(Exception e){paragraph("Section unavailable");}}
  void audio(){screen("Offline Audio");for(int i=1;i<=9;i++){final int id=i;action(String.format(java.util.Locale.US,"▶ Track %02d",id),()->play(id));}action("Stop",()->stopAudio());}
  void play(int n){stopAudio();try{AssetFileDescriptor fd=getAssets().openFd(String.format(java.util.Locale.US,"audio/track_%02d.mp3",n));player=new MediaPlayer();player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());fd.close();player.prepare();player.start();Toast.makeText(this,"Playing track "+n,Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Audio playback unavailable",Toast.LENGTH_LONG).show();}}
  void book(){screen("Original Offline PDF");try{
    File file=new File(getCacheDir(),"buddha-original.pdf");if(!file.isFile()||file.length()==0){InputStream in=getAssets().open("book.pdf");FileOutputStream out=new FileOutputStream(file);byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);out.close();in.close();}
    pdfFile=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);pdf=new PdfRenderer(pdfFile);
    action("Previous Page",()->pdfPage(page-1));action("Next Page",()->pdfPage(page+1));
    pdfLabel=text("",16);panel.addView(pdfLabel);pdfView=new ImageView(this);pdfView.setAdjustViewBounds(true);panel.addView(pdfView);pdfPage(0);
  }catch(Exception e){paragraph("Unable to open offline PDF: "+e.getMessage());}}
  void pdfPage(int p){if(pdf==null||p<0||p>=pdf.getPageCount())return;page=p;
    try(PdfRenderer.Page one=pdf.openPage(p)){float scale=Math.min(1.8f,900f/Math.max(1,one.getWidth()));Bitmap bmp=Bitmap.createBitmap(Math.max(1,(int)(one.getWidth()*scale)),Math.max(1,(int)(one.getHeight()*scale)),Bitmap.Config.ARGB_8888);bmp.eraseColor(-1);one.render(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);pdfView.setImageBitmap(bmp);pdfLabel.setText("Page "+(p+1)+" / "+pdf.getPageCount());}
    catch(Exception e){Toast.makeText(this,"PDF page error",Toast.LENGTH_SHORT).show();}}
  int qi,score;String[] qs={"Mindfulness is noticing ...","Compassion means ...","A teaching prompt is ..."};
  String[][] opts={{"Present moment","Only future plans","No attention"},{"Wishing well for others","Causing harm","Ignoring others"},{"Instructions to generate teaching","An official certificate","A completed lesson"}};
  void quiz(){qi=0;score=0;question();}
  void question(){screen("Practice · DEMO "+(qi+1)+"/3");paragraph(qs[qi]);
    for(int i=0;i<3;i++){final int answer=i;action(opts[qi][i],()->{if(answer==0)score++;qi++;if(qi<3)question();else result();});}}
  void result(){getSharedPreferences("practice",0).edit().putInt("score",score).apply();screen("Practice Result");paragraph("Score: "+score+"/3");paragraph("Demo only — no official course qualification or certificate issued.");action("Certificate Preview",()->certificate());}
  void certificate(){screen("Certificate · PREVIEW");paragraph("NOT VALID · NOT ISSUED");paragraph("The Admin Certificate Verification API has not been configured. Local quizzes do not issue certificates.");paragraph("Last demo score: "+getSharedPreferences("practice",0).getInt("score",0)+"/3");}
  void stopAudio(){if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();player=null;}}
  void closePdf(){if(pdf!=null){try{pdf.close();}catch(Exception ignored){}pdf=null;}if(pdfFile!=null){try{pdfFile.close();}catch(Exception ignored){}pdfFile=null;}}
  @Override protected void onDestroy(){stopAudio();closePdf();super.onDestroy();}
}