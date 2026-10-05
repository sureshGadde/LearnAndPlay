package com.example.kidsnumberquest.core

import com.example.kidsnumberquest.game.*
import kotlin.random.Random

class QuestionGenerator(private val random: Random = Random.Default) {
    private var lastAnswer: String? = null
    private fun pick(min: Int = 1, max: Int = 15): Int { var n:Int; do { n=random.nextInt(min,max+1) } while(n.toString()==lastAnswer); return n }
    private fun accept(q: Question): Question { lastAnswer=q.answer; return q }
    fun generate(module: Module, difficulty: Difficulty, count: Int = 30): List<Question> {
        lastAnswer=null
        return when(module){
            Module.BEFORE -> List(count){ val answer=pick(1,14); accept(Question("What comes before ${answer+1}?",answer.toString(),answer+1)) }
            Module.AFTER -> List(count){ val answer=pick(2,15); accept(Question("What comes after ${answer-1}?",answer.toString(),answer-1)) }
            Module.COMPARE -> List(count){
                var a=random.nextInt(1,16); var b=random.nextInt(1,16); while(a==b)b=random.nextInt(1,16)
                val op=if(a>b) ">" else "<"; while(op==lastAnswer){a=random.nextInt(1,16);b=random.nextInt(1,16);while(a==b)b=random.nextInt(1,16)}
                accept(Question("$a   ?   $b",if(a>b) ">" else "<",target=maxOf(a,b),left=a,right=b))
            }
            Module.BETWEEN -> List(count){ val middle=pick(2,14); accept(Question("What number is between ${middle-1} and ${middle+1}?",middle.toString(),middle,middle-1,middle+1)) }
            Module.SHAPES -> List(count){ var s:ShapeKind; do{s=ShapeKind.values()[random.nextInt(ShapeKind.values().size)]}while(s.name==lastAnswer); accept(Question("Find the ${s.name.lowercase()}",s.name,shape=s)) }
        }
    }
}
