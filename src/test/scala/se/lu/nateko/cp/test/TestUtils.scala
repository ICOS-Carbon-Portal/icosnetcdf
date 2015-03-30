package se.lu.nateko.cp.test

import java.io.File
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration


object TestUtils {

	def getFileInTarget(fileName: String) = new File(getClass.getResource("/").getFile + fileName)

	def time[T](code: => T): (Int, T) = {
		val start = System.currentTimeMillis
		val res = code
		val stop = System.currentTimeMillis
		((stop - start).toInt, res)
	}

	def time[T](code: => Unit): Int = {
		val start = System.currentTimeMillis
		val res = code
		val stop = System.currentTimeMillis
		(stop - start).toInt
	}

	
	def play: Unit = {
		
		val millis = time{
			val willBeString: Future[String] = Future{
				java.lang.Thread.sleep(1000)
				"bebe"
			}
			
			val willBeNumber: Future[Int] = Future{
				java.lang.Thread.sleep(500)
				42
			}
			
			val willBeUltimateResult = for(
				string <- willBeString;
				number <- willBeNumber
			)yield string + "#" + number 
		
			Await.result(willBeUltimateResult, Duration.Inf)
		}
		
		println(s"It took $millis milliseconds")
		
	}
	
}