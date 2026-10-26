package asia.aaii.soemoeacademy
import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
class MainActivity : Activity() {
 private val navy=Color.rgb(6,30,78)
 private val blue=Color.rgb(22,91,204)
 private val gold=Color.rgb(245,191,75)
 private val ink=Color.rgb(22,43,79)
 private val pale=Color.rgb(244,247,253)
 private val courses=listOf("English Basics","Business Skills","Digital Marketing","IT Fundamentals","Graphic Design","Communication Skills","Data Analysis","Project Management","Cloud Computing")
 private val symbols=listOf("📘","💼","📣","💻","🎨","🗣","📊","📋","☁")
 private var selected=0
 private lateinit var layout:LinearLayout
 private fun dp(i:Int)=(i*resources.displayMetrics.density).toInt()
 private fun bg(c:Int,r:Int=16)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
 private fun label(s:String,z:Float=14f,c:Int=ink,b:Boolean=false)=TextView(this).apply{text=s;textSize=z;setTextColor(c);if(b)setTypeface(null,Typeface.BOLD)}
 private fun column()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
 private fun horizontal()=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
 private fun spacer(n:Int)=View(this).apply{layoutParams=LinearLayout.LayoutParams(1,dp(n))}
 private fun padding(v:View,n:Int=12)=v.setPadding(dp(n),dp(n),dp(n),dp(n))
 private fun panel()=column().apply{background=bg(Color.WHITE);padding(this);elevation=dp(2).toFloat()}
 private fun heading(s:String){layout.addView(label(s,19f,ink,true));layout.addView(spacer(10))}
 private fun hero(s:String,detail:String){val box=column().apply{background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(navy,blue)).apply{cornerRadius=dp(20).toFloat()};padding(this,18)}
 box.addView(label("✦ SOE MOE ACADEMY",11f,gold,true));box.addView(spacer(9));box.addView(label(s,24f,Color.WHITE,true));box.addView(spacer(8));box.addView(label(detail,13f,Color.WHITE));layout.addView(box);layout.addView(spacer(18))}
 private fun tiles(items:List<String>,certificate:Boolean=false){
 val scroll=HorizontalScrollView(this).apply{isHorizontalScrollBarEnabled=false}
 val strip=horizontal();val cellWidth=(resources.displayMetrics.widthPixels-dp(47))/3
 items.forEach{ name-> val cell=panel();val i=courses.indexOf(name).coerceAtLeast(0)
 val picture=label(if(certificate)"🏅" else symbols[i%symbols.size],28f,blue).apply{gravity=Gravity.CENTER;background=bg(pale,12)}
 cell.addView(picture,LinearLayout.LayoutParams(-1,dp(67)));cell.addView(spacer(7));cell.addView(label(name,11f,ink,true).apply{minLines=2;maxLines=2});cell.addView(label(if(certificate)"✓ Completed" else "★ 4.8",10f,blue,true))
 cell.setOnClickListener{selected=i;show(if(certificate)"Certificate Detail" else "Course Detail")}
 strip.addView(cell,LinearLayout.LayoutParams(cellWidth,dp(151)).apply{rightMargin=dp(6)})
 }
 scroll.addView(strip);layout.addView(scroll);layout.addView(spacer(18))
 }
 private fun listItem(name:String,detail:String,target:String){val p=panel();p.addView(label(name,15f,ink,true));p.addView(spacer(5));p.addView(label(detail,12f,blue));p.setOnClickListener{show(target)};layout.addView(p);layout.addView(spacer(10))}
 private fun action(s:String,target:String){val b=label(s,15f,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=bg(blue,14);padding(this,14);setOnClickListener{show(target)}};layout.addView(b);layout.addView(spacer(14))}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=navy;window.navigationBarColor=navy;show("Home")}
 private fun show(page:String){
 val screen=column().apply{setBackgroundColor(pale)};setContentView(screen)
 val top=column().apply{setBackgroundColor(navy);padding(this,16)}
 top.addView(label("SM   SOE MOE Academy",19f,Color.WHITE,true));top.addView(spacer(7));top.addView(label(page,16f,gold,true));screen.addView(top)
 val scroll=ScrollView(this);layout=column().apply{padding(this,14)};scroll.addView(layout);screen.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
 when(page){
 "Home"->{hero("Learn. Achieve. Get Certified.","သင်ယူပါ • တိုးတက်ပါ • အောင်လက်မှတ်ရယူပါ");heading("My Certificates");tiles(courses.take(3),true);heading("Continue Learning");tiles(courses.take(6));heading("Popular Courses");tiles(courses)}
 "Courses"->{hero("Explore Courses","Your next learning journey starts here.");heading("Trending Courses");tiles(courses);heading("Recommended For You");tiles(courses.reversed());action("Search & Filter","Search")}
 "Course Detail"->{hero(courses[selected],"A professional learning experience");heading("Overview");listItem("Course Lessons","12 Lessons • 3h 20m","Lesson Player");listItem("Course Quiz","Check your understanding","Quiz");action("Start Learning","Lesson Player");heading("Related Courses");tiles(courses.take(6))}
 "Lesson Player"->{hero("▶ Lesson Player",courses[selected]);listItem("01 — Introduction","24 minutes • Now playing","Lesson Player");listItem("02 — Fundamentals","18 minutes","Lesson Player");listItem("03 — Practical Exercise","20 minutes","Lesson Player");action("Take Quiz","Quiz")}
 "Quiz"->{hero("Knowledge Check","Choose an answer to practice your knowledge");heading("What is your learning goal?");listItem("A. Build skills","Tap to see your results","Quiz Result");listItem("B. Continue improving","Tap to see your results","Quiz Result")}
 "Quiz Result"->{hero("Great work!","Your practice session is complete");action("Continue Learning","Courses")}
 "My Learning"->{hero("Learning Progress","Track your learning journey");heading("Continue Learning");tiles(courses.take(6));heading("Recently Viewed");tiles(courses.reversed())}
 "Certificates"->{hero("My Certificates","Your learning achievements");heading("Recent Certificates");tiles(courses.take(6),true);heading("Recommended Certifications");tiles(courses.drop(3))}
 "Certificate Detail"->{hero("Certificate of Completion",courses[selected]);heading("SOE MOE Academy");listItem("Certificate Preview","Sample certificate — demonstration only","Certificates");action("View Other Certificates","Certificates")}
 "Search"->{hero("Find Your Course","Explore categories and learning resources");heading("Popular Results");tiles(courses);heading("Categories");listItem("Business & Marketing","Explore courses","Courses");listItem("Language & Technology","Explore courses","Courses")}
 "Notifications"->{hero("Notifications","Learning updates and milestones");listItem("Course Reminder","Continue your learning journey","My Learning");listItem("Certificate Update","Review available certificates","Certificates");listItem("Recommended Courses","Discover new skills","Courses")}
 "Profile"->{hero("My Profile","SOE MOE Academy Learner");heading("My Account");listItem("Learning Progress","See your current courses","My Learning");listItem("My Certificates","View your achievements","Certificates");listItem("Notifications","Updates and reminders","Notifications");heading("Saved Courses");tiles(courses.take(3))}
 else->{hero(page,"SOE MOE Academy")}
 }
 val bar=horizontal().apply{setBackgroundColor(navy)}
 listOf("Home","Courses","Certificates","Profile").forEachIndexed{idx,name->
 val item=label(listOf("⌂","▣","★","♙")[idx]+"\n"+name,11f,if(name==page)gold else Color.WHITE,true).apply{gravity=Gravity.CENTER;padding(this,12);setOnClickListener{show(name)}}
 bar.addView(item,LinearLayout.LayoutParams(0,dp(63),1f))
 }
 screen.addView(bar)
 }
}
