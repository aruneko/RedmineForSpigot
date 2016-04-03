package net.aruneko.redmineforspigot

/**
  * Created by aruneko on 16/04/03.
  */

sealed trait MyEither[+E, +A] {
  def map[B](f: A => B): MyEither[E, B] = {
    this match {
      case Right(a) => Right(f(a))
      case Left(e) => Left(e)
    }
  }
  def flatMap[EE >: E, B](f: A => MyEither[EE, B]): MyEither[EE, B] = {
    this match {
      case Right(a) => f(a)
      case Left(e) => Left(e)
    }
  }
}
case class Left[+E](value: E) extends MyEither[E, Nothing]
case class Right[+A](value: A) extends MyEither[Nothing, A]
