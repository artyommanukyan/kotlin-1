class A {

}

fun A.foo(<caret>a: Int, b: String, c: Any) {

}

class B {
    fun bar(a: A) {
        a.foo(1, "1", "!")
    }
}