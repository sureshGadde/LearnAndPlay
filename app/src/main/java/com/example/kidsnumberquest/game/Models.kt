package com.example.kidsnumberquest.game

enum class Module(val title: String) { BEFORE("Before Numbers"), AFTER("After Numbers"), COMPARE("Greater / Less"), BETWEEN("In-Between"), SHAPES("Shapes") }
enum class Difficulty { EASY, HARD }
enum class ShapeKind { SQUARE, RECTANGLE, CIRCLE, OVAL, TRIANGLE }

data class Question(val prompt: String, val answer: String, val target: Int? = null, val left: Int? = null, val right: Int? = null, val shape: ShapeKind? = null)
