package eu.sesma.kuantum.cuanto.network


sealed class Either<out L, out R> {
    data class Left<L>(val t: L) : Either<L, Nothing>()
    data class Right<R>(val t: R) : Either<Nothing, R>()
}