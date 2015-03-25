package uk.gov.dvla.vehicles.assign.gatling

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.http.Predef._

final class Chains(data: RecordSeqFeederBuilder[String]) {

  def assetsAreAccessible =

  /* images */
    exec(
      http("apple-touch-icon-57x57.png")
        .get( s"""/assets/images/apple-touch-icon-57x57.png""")
    )
      .exec(
        http("apple-touch-icon-72x72.png")
          .get( s"""/assets/images/apple-touch-icon-72x72.png""")
      )
      .exec(
        http("apple-touch-icon-76x76.png")
          .get( s"""/assets/images/apple-touch-icon-76x76.png""")
      )
      .exec(
        http("apple-touch-icon-114x114.png")
          .get( s"""/assets/images/apple-touch-icon-114x114.png""")
      )
      .exec(
        http("apple-touch-icon-120x120.png")
          .get( s"""/assets/images/apple-touch-icon-120x120.png""")
      )
      .exec(
        http("apple-touch-icon-144x144.png")
          .get( s"""/assets/images/apple-touch-icon-144x144.png""")
      )
      .exec(
        http("apple-touch-icon-144x144.png")
          .get( s"""/assets/images/apple-touch-icon-152x152.png""")
      )
      .exec(
        http("document-reference-number.png")
          .get( s"""/assets/images/document-reference-number.png""")
      )
      .exec(
        http("favicon.ico")
          .get( s"""/assets/images/favicon.ico""")
      )
      .exec(
        http("gov-uk-email.jpg")
          .get( s"""/assets/images/gov-uk-email.jpg""")
      )
      .exec(
        http("govuk-crest.png")
          .get( s"""/assets/lib/vehicles-presentation-common/images/govuk-crest.png""")
          .headers(Map(
          """Accept""" -> """image/png,image/*;q=0.8,*/*;q=0.5""",
          """If-Modified-Since""" -> """Thu, 22 May 2014 14:25:18 GMT""",
          """If-None-Match""" -> """0464ba08d53d88645ca77f9907c082c8c10d563b"""))
      )
      .exec(
        http("pdf-icon-1.png")
          .get( s"""/assets/images/pdf-icon-1.png""")
      )
      .exec(
        http("v750-viewfinder-1.jpg")
          .get( s"""/assets/images/v750-viewfinder-1.jpg""")
      )
      .exec(
        http("v750-viewfinder-1.png")
          .get( s"""/assets/images/v750-viewfinder-1.png""")
      )
      .exec(
        http("v750-viewfinder-1-240px.jpg")
          .get( s"""/assets/images/v750-viewfinder-1-240px.jpg""")
      )
      .exec(
        http("v750-viewfinder-1-320px.jpg")
          .get( s"""/assets/images/v750-viewfinder-1-320px.jpg""")
      )
      .exec(
        http("v750-viewfinder-1-480px.jpg")
          .get( s"""/assets/images/v750-viewfinder-1-480px.jpg""")
      )
      .exec(
        http("v750-viewfinder-1-600px.jpg")
          .get( s"""/assets/images/v750-viewfinder-1-600px.jpg""")
      )
      .exec(
        http("v750-viewfinder-1-768px.jpg")
          .get( s"""/assets/images/v750-viewfinder-1-768px.jpg""")
      )
      .exec(
        http("v750-viewfinder-1-960px.jpg")
          .get( s"""/assets/images/v750-viewfinder-1-960px.jpg""")
      )
      .exec(
        http("V750_viewfinder-1.png")
          .get( s"""/assets/images/V750_viewfinder-1.png""")
      )
      .exec(
        http("v778-viewfinder-1.png")
          .get( s"""/assets/images/v778-viewfinder-1.png""")
      )
      .exec(
        http("v778-viewfinder-1-240px.jpg")
          .get( s"""/assets/images/v778-viewfinder-1-240px.jpg""")
      )
      .exec(
        http("v778-viewfinder-1-320px.jpg")
          .get( s"""/assets/images/v778-viewfinder-1-320px.jpg""")
      )
      .exec(
        http("v778-viewfinder-1-480px.jpg")
          .get( s"""/assets/images/v778-viewfinder-1-480px.jpg""")
      )
      .exec(
        http("v778-viewfinder-1-600px.jpg")
          .get( s"""/assets/images/v778-viewfinder-1-600px.jpg""")
      )
      .exec(
        http("v778-viewfinder-1-768px.jpg")
          .get( s"""/assets/images/v778-viewfinder-1-768px.jpg""")
      )
      .exec(
        http("v778-viewfinder-1-960px.jpg")
          .get( s"""/assets/images/v778-viewfinder-1-960px.jpg""")
      )
      .exec(
        http("V778_viewfinder-1.png")
          .get( s"""/assets/images/V778_viewfinder-1.png""")
      )

      /* javascript */
      .exec(
        http("capture-certificate-details-page.js")
          .get( s"""/assets/javascripts/capture-certificate-details-page.js""")
      )
      .exec(
        http("confirm-page.js")
          .get( s"""/assets/javascripts/confirm-page.js""")
      )
      .exec(
        http("firefox-dont-cache-page.js")
          .get( s"""/assets/javascripts/firefox-dont-cache-page.js""")
      )
      .exec(
        http("form-checked-selection.js")
          .get( s"""/assets/javascripts/form-checked-selection.js""")
      )
      .exec(
        http("header-footer-only.js")
          .get( s"""/assets/javascripts/header-footer-only.js""")
      )
      .exec(
        http("main.js")
          .get( s"""/assets/javascripts/main.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:10:42 GMT""",
          """If-None-Match""" -> """5f859f72e7cc426915cf32f2643ee5fc494b04a8"""))
      )
      .exec(
        http("on-event-change-display.js")
          .get( s"""/assets/javascripts/on-event-change-display.js""")
      )
      .exec(
        http("on-load-conditional-display.js")
          .get( s"""/assets/javascripts/on-load-conditional-display.js""")
      )
      .exec(
        http("picturefill.min.js")
          .get( s"""/assets/javascripts/picturefill.min.js""")
      )

      /* javascript from webjars */
      .exec(
        http("require.min.js")
          .get( s"""/webjars/requirejs/2.1.16/require.min.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Tue, 06 Aug 2013 09:49:32 GMT""",
          """If-None-Match""" -> """858bab5a8e8f73a1d706221ed772a4f740e168d5"""))
      )
      .exec(
        http("jquery.min.js")
          .get( s"""/webjars/jquery/1.9.1/jquery.min.js""")
          .headers(Map(
          """Accept""" -> """*/*""",
          """If-Modified-Since""" -> """Tue, 06 Aug 2013 09:49:32 GMT""",
          """If-None-Match""" -> """858bab5a8e8f73a1d706221ed772a4f740e168d5"""))
      )


      /* css */
      .exec(
        http("print.min.css")
          .get( s"""/assets/print.min.css""")
          .headers(Map(
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:08 GMT""",
          """If-None-Match""" -> """b2b112249c52769ac41acd83e388f550e4c39c6f"""))
      )
      .exec(
        http("screen.min.css")
          .get( s"""/assets/screen.min.css""")
          .headers(Map(
          """If-Modified-Since""" -> """Thu, 05 Jun 2014 21:08:06 GMT""",
          """If-None-Match""" -> """59f34576dba4629e6e960e1d514fe573775e9999"""))
      )
}
