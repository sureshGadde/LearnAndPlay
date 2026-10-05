package com.example.kidsnumberquest.game

import com.example.kidsnumberquest.core.QuestionGenerator

class GameEngine(private val generator: QuestionGenerator = QuestionGenerator()) {
    var module=Module.BEFORE; var difficulty=Difficulty.EASY; var index=0; var questions=generator.generate(module,difficulty); var answered=false
        private set
    fun start(m:Module,d:Difficulty){ module=m; difficulty=d; index=0; questions=generator.generate(m,d); answered=false }
    val current get()=questions[index]
    fun check(answer:String):Boolean=answer.trim().equals(current.answer,ignoreCase=true).also{ if(it) answered=true }
    fun next(){ if(index<questions.lastIndex){ index++; answered=false } }
}
