package com.example.kidsnumberquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsnumberquest.core.*
import com.example.kidsnumberquest.game.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { KidsNumberQuestApp() } }
}

@Composable fun KidsNumberQuestApp(){
    val engine=remember{GameEngine()}; var module by remember{mutableStateOf(Module.BEFORE)}; var difficulty by remember{mutableStateOf(Difficulty.EASY)}
    var refresh by remember{mutableIntStateOf(0)}
    MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF6C63FF),secondary=Color(0xFFFFB703),background=Color(0xFFFFFBF0))){
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background){
            Column(Modifier.fillMaxSize().padding(12.dp)){
                Text("Kids Number Quest",fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF4B3F72),modifier=Modifier.padding(8.dp))
                ModuleTabs(module){ module=it; engine.start(it,difficulty); refresh++ }
                DifficultyTabs(difficulty){ difficulty=it; engine.start(module,it); refresh++ }
                key(refresh){ GameScreen(engine) }
            }
        }
    }
}

@Composable private fun ModuleTabs(selected:Module,onSelect:(Module)->Unit){
    Row(Modifier.fillMaxWidth().padding(bottom=6.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)){
        Module.values().forEach{m->FilterChip(selected==m,onClick={onSelect(m)},label={Text(m.title,fontSize=11.sp)})}
    }
}
@Composable private fun DifficultyTabs(selected:Difficulty,onSelect:(Difficulty)->Unit){
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(bottom=6.dp)){
        FilterChip(selected==Difficulty.EASY,{onSelect(Difficulty.EASY)},{Text("Level 1 • Easy")})
        FilterChip(selected==Difficulty.HARD,{onSelect(Difficulty.HARD)},{Text("Level 2 • Challenge")})
    }
}

@Composable private fun GameScreen(engine:GameEngine){
    var strokes by remember{mutableStateOf<List<List<StrokePoint>>>(emptyList())}; var currentStroke by remember{mutableStateOf<List<StrokePoint>>(emptyList())}
    var message by remember{mutableStateOf("")}; var wiggle by remember{mutableFloatStateOf(0f)}; var reward by remember{mutableStateOf(false)}; val scope=rememberCoroutineScope(); val context=LocalContext.current
    val audio=remember{GameAudio(context)}
    DisposableEffect(Unit){onDispose{audio.release()}}
    fun clear(){strokes=emptyList();currentStroke=emptyList();message=""}
    fun submit(value:String){ if(engine.answered)return; if(engine.check(value)){message="Amazing!";reward=true;audio.correct();scope.launch{delay(3000);reward=false}} else {message="Almost! Try again.";scope.launch{wiggle=12f;repeat(4){wiggle=-wiggle;delay(55)};wiggle=0f}} }
    Column(Modifier.fillMaxSize().rotate(wiggle),horizontalAlignment=Alignment.CenterHorizontally){
        LinearProgressIndicator(progress={(engine.index+1)/30f},modifier=Modifier.fillMaxWidth().height(8.dp),color=Color(0xFF6C63FF))
        Text("Question ${engine.index+1} of 30",modifier=Modifier.padding(4.dp),fontWeight=FontWeight.Bold)
        when(engine.module){
            Module.BEFORE,Module.AFTER,Module.COMPARE,Module.BETWEEN -> NumberQuestion(engine,strokes,currentStroke,{p->currentStroke=p},{s->strokes=s},::clear,::submit,audio)
            Module.SHAPES -> ShapeQuestion(engine,::submit,audio)
        }
        if(message.isNotEmpty()&&!engine.answered) Text(message,fontSize=21.sp,fontWeight=FontWeight.Bold,color=Color(0xFF7A6F9B),modifier=Modifier.padding(8.dp))
        if(engine.answered){ if(!reward){ Button(onClick={engine.next();clear();message=""},modifier=Modifier.padding(8.dp)){Text(if(engine.index==29)"Finish Level 🎉" else "Next Question →",fontSize=20.sp)} } }
        if(reward) Confetti()
    }
}

@Composable private fun NumberQuestion(engine:GameEngine,strokes:List<List<StrokePoint>>,currentStroke:List<StrokePoint>,setCurrent:(List<StrokePoint>)->Unit,setStrokes:(List<List<StrokePoint>>)->Unit,clear:()->Unit,submit:(String)->Unit,audio:GameAudio){
    val q=engine.current
    Spacer(Modifier.height(6.dp)); Text(q.prompt,fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF40355D));
    if(engine.difficulty==Difficulty.EASY && engine.module!=Module.COMPARE){ NumberGuide(target=q.target?:q.left?:1) }
    if(engine.module==Module.COMPARE){
        Text("Drag the symbol to the ?",fontSize=19.sp,modifier=Modifier.padding(6.dp)); DragSymbol(answer=q.answer,submit=submit)
    } else {
        if(engine.difficulty==Difficulty.EASY && engine.module==Module.BETWEEN) NumberGuide(target=q.target?:3)
        if(engine.module==Module.BETWEEN) ArrowHint()
        HandwritingBox(strokes,currentStroke,setCurrent,setStrokes,submit,clear,expected=q.answer)
    }
    Text(if(engine.difficulty==Difficulty.EASY)"You can use the number path to help." else "Draw gently inside the box.",fontSize=15.sp,color=Color.Gray)
}

@Composable private fun NumberGuide(target:Int){
    Row(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
        (1..15).forEach{n->Box(Modifier.size(29.dp).background(if(n==target)Color(0xFFFFD166) else Color.Transparent,CircleShape),contentAlignment=Alignment.Center){Text("$n",fontWeight=if(n==target)FontWeight.ExtraBold else FontWeight.Normal)}}
    }
}
@Composable private fun ArrowHint(){ Canvas(Modifier.size(150.dp,55.dp)){drawArc(Color(0xFF6C63FF),180f,180f,false,style=androidx.compose.ui.graphics.drawscope.Stroke(6f));drawLine(Color(0xFF6C63FF),Offset(size.width-18,size.height/2),Offset(size.width-35,size.height/2-12),6f);drawLine(Color(0xFF6C63FF),Offset(size.width-18,size.height/2),Offset(size.width-35,size.height/2+12),6f)} }

@Composable private fun HandwritingBox(strokes:List<List<StrokePoint>>,current:List<StrokePoint>,setCurrent:(List<StrokePoint>)->Unit,setStrokes:(List<List<StrokePoint>>)->Unit,submit:(String)->Unit,clear:()->Unit,expected:String){
    val recognizer=remember{HandwritingRecognizer()}
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Box(Modifier.fillMaxWidth().height(220.dp).padding(12.dp).background(Color.White,RoundedCornerShape(24.dp)).pointerInput(Unit){detectDragGestures(onDragStart={o->setCurrent(listOf(StrokePoint(o.x,o.y)))},onDrag={change,_->change.consume();setCurrent(current+StrokePoint(change.position.x,change.position.y))},onDragEnd={setStrokes(strokes+listOf(current));setCurrent(emptyList())})}){
            Canvas(Modifier.fillMaxSize()) { strokes.forEach{pts->for(i in 1 until pts.size)drawLine(Color(0xFF4B3F72),Offset(pts[i-1].x,pts[i-1].y),Offset(pts[i].x,pts[i].y),10f,StrokeCap.Round)}; for(i in 1 until current.size)drawLine(Color(0xFF4B3F72),Offset(current[i-1].x,current[i-1].y),Offset(current[i].x,current[i].y),10f,StrokeCap.Round) }
            Text("Draw here",color=Color(0xFFB8B0C8),fontSize=20.sp,modifier=Modifier.align(Alignment.Center))
        }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onClick=clear){Text("Clear / Try Again")};Button(onClick={val r=recognizer.recognize(strokes);if(r.confidence>=.45f&&r.value!=null)submit(r.value!!)else clear()}){Text("Check")}}
    }
}

@Composable private fun DragSymbol(answer:String,submit:(String)->Unit){
    var offset by remember{mutableStateOf(Offset.Zero)}
    Row(horizontalArrangement=Arrangement.spacedBy(30.dp),verticalAlignment=Alignment.CenterVertically){
        listOf("<",">").forEach{symbol->Box(Modifier.size(82.dp).offset((offset.x/2).dp,(offset.y/2).dp).background(Color(0xFFE6E0FF),RoundedCornerShape(22.dp)).pointerInput(symbol){detectDragGestures(onDrag={c,d->c.consume();offset+=d},onDragEnd={if(offset.getDistance()>60)submit(symbol);offset=Offset.Zero})},contentAlignment=Alignment.Center){Text(symbol,fontSize=48.sp,fontWeight=FontWeight.ExtraBold)}}
    }
}

@Composable private fun ShapeQuestion(engine:GameEngine,submit:(String)->Unit,audio:GameAudio){
    val q=engine.current; LaunchedEffect(q){audio.speak(q.prompt)}
    Text(q.prompt,fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF40355D),modifier=Modifier.padding(8.dp))
    if(engine.difficulty==Difficulty.EASY){
        Text("Tap the shape to color it!",fontSize=18.sp); Row(Modifier.fillMaxWidth().height(260.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){ShapeKind.values().take(3).forEach{shape->ShapeCard(shape){submit(shape.name)}}}
    } else {
        Text("Tap the real-world object with this shape.",fontSize=18.sp); Row(Modifier.fillMaxWidth().height(260.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){ShapeKind.values().forEach{shape->ObjectCard(shape){submit(shape.name)}}}
    }
}
@Composable private fun ShapeCard(shape:ShapeKind,onTap:()->Unit){Box(Modifier.size(105.dp).pointerInput(shape){detectTapGestures{onTap()}}){Canvas(Modifier.fillMaxSize()){drawShape(shape,center,Color(0xFFFFD166))}}}
@Composable private fun ObjectCard(shape:ShapeKind,onTap:()->Unit){Box(Modifier.size(115.dp).background(Color.White,RoundedCornerShape(20.dp)).pointerInput(shape){detectTapGestures{onTap()}},contentAlignment=Alignment.Center){Text(when(shape){ShapeKind.RECTANGLE->"📱";ShapeKind.OVAL->"🥚";ShapeKind.CIRCLE->"⚽";ShapeKind.SQUARE->"🖼️";ShapeKind.TRIANGLE->"⛺"},fontSize=48.sp)}}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShape(s:ShapeKind,c:Offset,color:Color){when(s){ShapeKind.CIRCLE->drawCircle(color,48f,c);ShapeKind.SQUARE->drawRect(color,topLeft=Offset(c.x-42,c.y-42),size=androidx.compose.ui.geometry.Size(84f,84f));ShapeKind.RECTANGLE->drawRect(color,topLeft=Offset(c.x-48,c.y-30),size=androidx.compose.ui.geometry.Size(96f,60f));ShapeKind.OVAL->drawOval(color,topLeft=Offset(c.x-38,c.y-50),size=androidx.compose.ui.geometry.Size(76f,100f));ShapeKind.TRIANGLE->drawPath(Path().apply{moveTo(c.x,c.y-50);lineTo(c.x+50,c.y+45);lineTo(c.x-50,c.y+45);close()},color)}}

@Composable private fun Confetti(){
    val progress=remember{Animatable(0f)}; LaunchedEffect(Unit){progress.animateTo(1f,tween(3000))}
    Canvas(Modifier.fillMaxSize()){repeat(70){i->val x=((i*83)%1000)/1000f*size.width;val y=(progress.value*size.height+(i*37)%200)%size.height;val s=6f+(i%6);drawRect(Color.hsv((i*47)%360f,.75f,1f),Offset(x,y),androidx.compose.ui.geometry.Size(s,s*1.7f))}}
}
