interface _EmptyInterface

interface _Interface1 {
    fun itest() {}
    fun itest1() {}
}

interface _Interface2 {
    fun itest() {}
    fun itest2() {}
}

interface _Interface3 {
    fun itest() {}
    fun itest3() {}
}

interface _InterfaceWithOutParameter<out T>

interface _InterfaceWithTypeParameter1<T> {
    fun ip1test1(): T? = null as T?
}

interface _InterfaceWithTypeParameter2<T> {
    fun ip1test2(): T? = null as T?
}

interface _InterfaceWithTypeParameter3<T> {
    fun ip1test3(): T? = null as T?
}

interface _InterfaceWithFiveTypeParameters1<T1, T2, T3, T4, T5> {
    fun itest() {}
    fun itest1() {}
}

interface _InterfaceWithFiveTypeParameters2<T1, T2, T3, T4, T5> {
    fun itest() {}
    fun itest2() {}
}

interface _InterfaceWithFiveTypeParameters3<T1, T2, T3, T4, T5> {
    fun itest() {}
    fun itest3() {}
}

interface _InterfaceWithTwoTypeParameters<T, K> {
    fun ip2test(): T? = null as T?
}
