import zio.http.*
import zio.http.netty.NettyConfig

object TestConfig extends App {
  // Test what methods are available on Server.Config
  val config = Server.Config.default
  println("Server.Config methods:")
  config.getClass.getMethods.foreach(m => 
    if (m.getName.contains("max") || m.getName.contains("size") || m.getName.contains("content")) {
      println(s"  ${m.getName}: ${m.getReturnType}")
    }
  )
  
  // Test what methods are available on NettyConfig  
  val nettyConfig = NettyConfig.default
  println("\nNettyConfig methods:")
  nettyConfig.getClass.getMethods.foreach(m => 
    if (m.getName.contains("max") || m.getName.contains("size") || m.getName.contains("content") || m.getName.contains("aggregator")) {
      println(s"  ${m.getName}: ${m.getReturnType}")
    }
  )
}