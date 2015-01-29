package composition

import com.tzavellas.sse.guice.ScalaModule
import email.{EmailService, EmailServiceImpl}

/**
 * Provides real implementations of traits
 * Note the use of sse-guice, which is a library that makes the Guice internal DSL more scala friendly
 * eg we can write this:
 * bind[Service].to[ServiceImpl].in[Singleton]
 * instead of this:
 * bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *
 * Look in build.scala for where we import the sse-guice library
 */
final class DevModule extends ScalaModule {

  def configure() {
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[audit1.AuditService].to[audit1.AuditLocalServiceImpl].asEagerSingleton()
    bind[RefererFromHeader].to[RefererFromHeaderImpl].asEagerSingleton()
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
  }
}