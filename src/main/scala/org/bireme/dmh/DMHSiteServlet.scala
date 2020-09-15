/*=========================================================================

    DeCSMeSHHighlighter © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHHighlighter/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmh

import java.io.{InputStream, PrintWriter}

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import javax.servlet.{ServletConfig, ServletContext}

import scala.util.{Failure, Success, Try}

class DMHSiteServlet extends HttpServlet {
    var i18n: I18N = _

    /**
      * Do initial web app configuration
      * @param config servlet config object
      */
    override def init(config: ServletConfig): Unit = {
      super.init(config)

      val context: ServletContext = config.getServletContext
      val i18nIS: InputStream = context.getResourceAsStream("/i18n.txt")

      i18n = new I18N(i18nIS)

      println("DMHSiteServlet is listening ...")
    }

    /**
      * Process get http requisition
      * @param request http request object
      * @param response http response object
      */
    override def doGet(request: HttpServletRequest,
                       response: HttpServletResponse): Unit = processRequest(request, response)

    /**
      * Process post http requisition
      * @param request http request object
      * @param response http response object
      */
    override def doPost(request: HttpServletRequest,
                        response: HttpServletResponse): Unit = processRequest(request, response)

    /**
      *  Process get or post requisition
      * @param request http request object
      * @param response http response object
      */
    private def processRequest(request: HttpServletRequest,
                               response: HttpServletResponse): Unit = {
      response.setCharacterEncoding("utf-8")

      Try {
        val language: String = Option(request.getParameter("lang")).map(_.trim)
          .map(l => if (l.isEmpty) "en" else l).getOrElse("en")
//println(s"par_lang=${request.getParameter("lang")} language=$language")
        val outputText: String = getHtml(language)
        val out: PrintWriter = response.getWriter
        out.println(outputText)
        out.flush()
      } match {
        case Success(_) => ()
        case Failure(ex) => response.sendError(500, ex.getMessage)
      }
    }

  private def getHtml(language: String): String = {
    """
<!DOCTYPE html>
<html lang="""" + language + """">
   <head>
      <!-- Global site tag (gtag.js) - Google Analytics -->
      <script async src="https://www.googletagmanager.com/gtag/js?id=UA-39600115-37"></script>
      <script>
        window.dataLayer = window.dataLayer || [];
        function gtag(){dataLayer.push(arguments);}
        gtag('js', new Date());

        gtag('config', 'UA-39600115-37');
      </script>

      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <meta name="Keywords" content="">
      <meta name="Description" content="">
      <meta name="Author" content="">
      <title>DeCS Highlighter</title>
      <link rel="stylesheet" href="siteDeCSh/css/aos.css">
      <link rel="stylesheet" href="siteDeCSh/css/bootstrap.min.css">
      <link rel="stylesheet" href="siteDeCSh/css/fontawesome/css/all.css">
      <link rel="stylesheet" href="siteDeCSh/css/accessibility.css">
      <link rel="stylesheet" href="siteDeCSh/css/style.css">
      <link rel="stylesheet" href="siteDeCSh/css/aos.css">
      <link rel="shortcut icon" href="siteDeCSh/img/favicon.png">
   </head>
   <body id="body">
      <script type="text/javascript">
		     function submitPage(plang, toWizard) {
////alert("plang=" + plang + " toWizard="  + toWizard);
	          var pageLang = """" + language + """";
            var language;
            if (plang === "") language = pageLang;
            else language = plang;

            var form = document.createElement("form");
            form.setAttribute("method", "post");
            if (toWizard) {
              form.setAttribute("action", "dmhw");
              form.setAttribute("target", "_blank");
            }
            else form.setAttribute("action", "dmhs");

            var hiddenField1 = document.createElement("input");
            hiddenField1.setAttribute("type", "hidden");
            hiddenField1.setAttribute("name", "lang");
            hiddenField1.setAttribute("value", language);
            form.appendChild(hiddenField1);

            document.body.appendChild(form);

            form.submit();
		     }
	    </script>

      <section id="barAccessibility">
        <div class="container">
          <div class="row">
            <div class="col-md-6" id="accessibilityTutorial">
              <a href="#main_container" tabindex="1" role="button">""" + i18n.translate("Main Content", language) + """ <span class="hiddenMobile">1</span></a>
              <!-- <a href="#nav" tabindex="2" role="button">Menu <span class="hiddenMobile">2</span></a>
              <a href="#fieldSearch" tabindex="3" id="accessibilitySearch" role="button">""" + i18n.translate("Search", language) + """ <span class="hiddenMobile">3</span></a> -->
              <a href="#footer" tabindex="4" role="button">""" + i18n.translate("Footer", language) + """ <span class="hiddenMobile">4</span></a>
            </div>
            <div class="col-md-6" id="accessibilityFontes">
              <a href="#!" id="fontPlus"  tabindex="5" aria-hidden="true">+A</a>
              <a href="#!" id="fontNormal"  tabindex="6" aria-hidden="true">A</a>
              <a href="#!" id="fontLess"  tabindex="7" aria-hidden="true">-A</a>
              <a href="#!" id="contraste"  tabindex="8" aria-hidden="true"><i class="fas fa-adjust"></i> """ + i18n.translate("High Contrast", language) + """</a>
              <a href="https://politicas.bireme.org/accesibilidad/""" + (if (language.equals("fr")) "en" else language) + """" role="button" id="accebilidade" tabindex="9" target="_blank" title='""" + i18n.translate("Accessibility", language) + """'><i class="fas fa-wheelchair"></i></a>
            </div>
          </div>
        </div>
      </section>

      <header id="header">
         <div class="container">
            <div class="row" style="position: relative;">
               <div id="language">
                  <a href="#" onclick='submitPage("en", false);'>English</a>
                  <a href="#" onclick='submitPage("es", false);'>Español</a>
                  <a href="#" onclick='submitPage("pt", false);'>Português</a>
                  <a href="#" onclick='submitPage("fr", false);'>Français</a>
               </div>
               <div id="brand" class="col-md-12 text-center">
                  <img src="siteDeCSh/img/logo.svg" alt="DeCSMeshHiglighter">
               </div>
            </div>
            <div class="row">
               <div class="col-md-12 text-center" id="textTop">
                  <h1>""" + i18n.translate("Automatically find all DeCS/MeSH terms in your document", language) + """.</h1>
               </div>
            </div>
         </div>
      </header>
      <section class="padding1" id="howToSection">
         <div class="container" id="boxUse">View your result
            <div class="row" id="howTo">
               <div class="col-12">
                  <h3 class=" text-center">""" + i18n.translate("How to use", language) + """?</h3>
                  <hr class="default_divider">
               </div>
               <div class="col-md-4">
                  <img src="siteDeCSh/img/copy.svg" alt="">
                  <div class="howToText">
                     <b>""" + i18n.translate("Select your Text", language) + """</b>
                     <p>""" + i18n.translate("Select the text in which you want to find the DeCS/MeSH terms", language) + """.</p>
                  </div>
               </div>
               <div class="col-md-4">
                  <img src="siteDeCSh/img/paste.svg" alt="">
                  <div class="howToText">
                     <b>""" + i18n.translate("Paste your Text", language) + """</b>
                     <p>""" + i18n.translate("Enter the selected text on the application page", language) + """.</p>
                  </div>
               </div>
               <div class="col-md-4">
                  <img src="siteDeCSh/img/result.svg" alt="">
                  <div class="howToText">
                     <b>""" + i18n.translate("View your result", language) + """</b>
                     <p>""" + i18n.translate("Get the list of localized DeCS/MeSH terms instantly", language) + """.</p>
                  </div>
               </div>
            </div>
         </div>
         <div class="text-center">
            <a href="javascript:submitPage('""" + language + """', true);" class="btn btn-lg btn-warning">""" + i18n.translate("Try it now", language) + """</a>
         </div>
      </section>
      <section class="padding1" id="why">
         <div class="container" id="main_container">
            <div class="row">
               <div class="col-md-12">
                  <h2 class="title1 text-center">""" + i18n.translate("Why use DeCS/MeSH Highlighter", language) + """?</h2>
                  <hr class="default_divider default_divider2">
                  <p>""" + i18n.translate("Determining the keywords", language) + """</p>
                  <br>
               </div>
            </div>
         </div>
      </section>
      <section class="padding1">
         <div class="container">
            <h2 class="title text-center">""" + i18n.translate("Technical Features", language) + """</h2>
            <hr class="default_divider">
            <div class="row">
               <div class="col-md-8">
                  <ul class="nav nav-tabs" id="feature" role="tablist">
                     <li class="nav-item" role="presentation">
                        <a class="nav-link active" id="home-tab" data-toggle="tab" href="#home" role="tab" aria-controls="home" aria-selected="true">""" + i18n.translate("SIMPLE", language) + """</a>
                     </li>
                     <li class="nav-item" role="presentation">
                        <a class="nav-link" id="profile-tab" data-toggle="tab" href="#profile" role="tab" aria-controls="profile" aria-selected="false">""" + i18n.translate("ADVANCED", language) + """</a>
                     </li>
                  </ul>
                  <div class="tab-content" id="featureContent">
                     <div class="tab-pane fade show active" id="home" role="tabpanel" aria-labelledby="home-tab">
                        <p>""" + i18n.translate("The basic DeCS/MeSH Highlighter interface", language) + """</p>
                        <!--a href="dmhw?lang=""" + language + """" target="_blank" class="btn btn-lg btn-outline-success">""" + i18n.translate("Try it now", language) + """</a-->
                        <a href="javascript:submitPage('""" + language + """', true);" class="btn btn-lg btn-outline-success">""" + i18n.translate("Try it now", language) + """</a>
                     </div>
                     <div class="tab-pane fade" id="profile" role="tabpanel" aria-labelledby="profile-tab">
                        <p>""" + i18n.translate("The advanced interface of DeCS/MeSH Highlighter", language) + """</p>
                        <!--a href="dmhw?lang=""" + language + """" target="_blank" class="btn btn-lg btn-outline-success">""" + i18n.translate("Try it now", language) + """</a-->
                        <a href="javascript:submitPage('""" + language + """', true);" class="btn btn-lg btn-outline-success">""" + i18n.translate("Try it now", language) + """</a>
                     </div>
                  </div>
               </div>
               <div class="col-md-4">
                  <img src="siteDeCSh/img/notebook.jpg" alt="" class="img-fluid">
               </div>
            </div>
         </div>
      </section>
      <section class="padding1 color2">
         <div class="container">
            <div class="row">
               <div class="col-md-12">
                  <h2 class="title text-center textWhite">FAQ</h2>
                  <hr class="default_divider default_divider2">
                  <div class="accordion" id="accordionFAQ">
                     <div class="card">
                        <div class="card-header" id="headingOne">
                           <h2 class="mb-0">
                              <button class="btn btn-link btn-block text-left" type="button" data-toggle="collapse" data-target="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
                              """ + i18n.translate("How does DeCS/MeSH Highlighter find terms", language) + """?
                              </button>
                           </h2>
                        </div>
                        <div id="collapseOne" class="collapse show" aria-labelledby="headingOne" data-parent="#accordionFAQ">
                           <div class="card-body">
                              """ + i18n.translate("DeCS/MeSH Highlighter goes through each word", language) + """
                           </div>
                        </div>
                     </div>
                     <div class="card">
                        <div class="card-header" id="headingTwo">
                           <h2 class="mb-0">
                              <button class="btn btn-link btn-block text-left collapsed" type="button" data-toggle="collapse" data-target="#collapseTwo" aria-expanded="false" aria-controls="collapseTwo">
                              """ + i18n.translate("Is DeCS/MeSH Highlighter free to use", language) + """?
                              </button>
                           </h2>
                        </div>
                        <div id="collapseTwo" class="collapse" aria-labelledby="headingTwo" data-parent="#accordionFAQ">
                           <div class="card-body">
                              """ + i18n.translate("Yes, using DeCS/MeSH Highlighter is free", language) + """
                           </div>
                        </div>
                     </div>
                     <div class="card">
                        <div class="card-header" id="headingThree">
                           <h2 class="mb-0">
                              <button class="btn btn-link btn-block text-left collapsed" type="button" data-toggle="collapse" data-target="#collapseThree" aria-expanded="false" aria-controls="collapseThree">
                              """ + i18n.translate("Who are the main users of DeCS/MeSH Highlighter", language) + """?
                              </button>
                           </h2>
                        </div>
                        <div id="collapseThree" class="collapse" aria-labelledby="headingThree" data-parent="#accordionFAQ">
                           <div class="card-body">
                              """ + i18n.translate("Three typical users of the tool are", language) + """
                           </div>
                        </div>
                     </div>
                     <div class="card">
                        <div class="card-header" id="headingFour">
                           <h2 class="mb-0">
                              <button class="btn btn-link btn-block text-left collapsed" type="button" data-toggle="collapse" data-target="#collapseFour" aria-expanded="false" aria-controls="collapseFour">
                              """ + i18n.translate("How often is the DeCS/MeSH thesaurus updated", language) + """?
                              </button>
                           </h2>
                        </div>
                        <div id="collapseFour" class="collapse" aria-labelledby="headingFour" data-parent="#accordionFAQ">
                           <div class="card-body">
                              """ + i18n.translate("The DeCS/MesH thesaurus is updated once a year, and it is released at the beginning of each year.", language) + """
                           </div>
                        </div>
                     </div>
                     <div class="card">
                        <div class="card-header" id="headingFive">
                           <h2 class="mb-0">
                              <button class="btn btn-link btn-block text-left collapsed" type="button" data-toggle="collapse" data-target="#collapseFive" aria-expanded="false" aria-controls="collapseFive">
                              """ + i18n.translate("How do I highlight the DeCS/MeSH terms on my web pages", language) + """?
                              </button>
                           </h2>
                        </div>
                        <div id="collapseFive" class="collapse" aria-labelledby="headingFive" data-parent="#accordionFAQ">
                           <div class="card-body">
                             """ + i18n.translate("Your web pages", language) + """
                             <br/><br/>
                             <!-- livezilla.net PLACE WHERE YOU WANT TO SHOW TEXT LINK -->
                             <a class="btn btn-outline-light lz_text_link" href="javascript:void(window.open('//contacto.bvsalud.org/chat.php?operator=morimarc&group=DeCSMeSH%20HighLighter&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__&ovltwo=MQ__','','width=400,height=600,left=0,top=0,resizable=yes,menubar=no,location=no,status=yes,scrollbars=yes'))" alt="LiveZilla Live Chat Software" data-text-online="""" + i18n.translate("DeCS/MeSH Highlighter Online Service", language) + """" data-text-offline="""" + i18n.translate("DeCS/MeSH Highlighter Online Service", language) + """" data-css-online="" data-css-offline="" data-online-only="0">""" + i18n.translate("DeCS/MeSH Highlighter Online Service", language) + """</a>
                             <!-- livezilla.net PLACE WHERE YOU WANT TO SHOW TEXT LINK -->
                           </div>
                        </div>
                     </div>
                     <div class="card">
                        <div class="card-header" id="headingSix">
                           <h2 class="mb-0">
                              <button class="btn btn-link btn-block text-left collapsed" type="button" data-toggle="collapse" data-target="#collapseSix" aria-expanded="false" aria-controls="collapseSix">
                              """ + i18n.translate("How do I find DeCS/MeSH terms in many documents", language) + """?
                              </button>
                           </h2>
                        </div>
                        <div id="collapseSix" class="collapse" aria-labelledby="headingSix" data-parent="#accordionFAQ">
                           <div class="card-body">
                              """ + i18n.translate("Your documents may have the terms DeCS/MeSH highlighted", language) + """
                              <br/><br/>
                              <!-- livezilla.net PLACE WHERE YOU WANT TO SHOW TEXT LINK -->
                              <a class="btn btn-outline-light lz_text_link" href="javascript:void(window.open('//contacto.bvsalud.org/chat.php?operator=morimarc&group=DeCSMeSH%20HighLighter&hg=Pw__&ptl=""" + (if (language.equals("fr")) "en" else language) + """&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__&ovltwo=MQ__','','width=400,height=600,left=0,top=0,resizable=yes,menubar=no,location=no,status=yes,scrollbars=yes'))" alt="LiveZilla Live Chat Software" data-text-online="""" + i18n.translate("DeCS/MeSH Highlighter Online Service", language) + """" data-text-offline="""" + i18n.translate("DeCS/MeSH Highlighter Online Service", language) + """" data-css-online="" data-css-offline="" data-online-only="0">""" + i18n.translate("DeCS/MeSH Highlighter Online Service", language) + """</a>
                              <!-- livezilla.net PLACE WHERE YOU WANT TO SHOW TEXT LINK -->
                           </div>
                           </div>
                        </div>
                     </div>
                  </div>
               </div>
            </div>
         </div>
      </section>
      <section class="padding1">
         <div class="container">
            <div class="row">
               <div class="col-md-8 col-lg-6 text-center" id="siteDeCS">
                  <h1>
                     <a href="https://beta.decs.bvsalud.org/""" + (if (language.equals("pt")) "" else language) + """" class="" target="_blank">""" + i18n.translate("Access the DeCS/MeSH website", language) + """.</a>
                  </h1>
               </div>
               <div class="col-md-4 col-lg-6 text-center">
                  <img src="siteDeCSh/img/siteDeCS.png" alt="Site DeCS/MeSH" class="img-fluid">
               </div>
            </div>
         </div>
      </section>

      <footer id="footer" class="padding1">
          <div class="container">
              <div class="row">
                  <div class="col-md-5">
                      <b>DeCS/MeSH Highlighter</b> <br>
                      <a href="http://politicas.bireme.org/terminos/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Terms and conditions of use", language) + """</a>
                      <a href="http://politicas.bireme.org/privacidad/""" + (if (language.equals("fr")) "en" else language) + """" target="_blank">""" + i18n.translate("Privacy policy", language) + """</a>
                  </div>
                  <div class="col-md-7 text-right">
                      <img src="http://logos.bireme.org/img/""" + language + """/h_bir_white.svg" alt="" class="img-fluid">
                  </div>
              </div>
          </div>
      </footer>

      <script src="siteDeCSh/js/jquery-3.5.1.min.js"></script>
      <script src="siteDeCSh/js/bootstrap.min.js"></script>
      <script src="siteDeCSh/js/cookie.js"></script>
      <script src="siteDeCSh/js/accessibility.js"></script>
      <script src="siteDeCSh/js/aos.js"></script>
      <script src="siteDeCSh/js/main.js"></script>

      <!-- livezilla.net PLACE SOMEWHERE IN BODY -->
      <!-- PASS THRU DATA OBJECT -->
      <script type="text/javascript">
      var lz_data = {overwrite:false,language:'en', textlink:true};
      </script>
      <!-- PASS THRU DATA OBJECT -->

      <div id="lvztr_de9" style="display:none"></div><script id="lz_r_scr_74aeee9c36e59281529443e9e1a975d3" type="text/javascript" defer>lz_code_id="74aeee9c36e59281529443e9e1a975d3";var script = document.createElement("script");script.async=true;script.type="text/javascript";var src = "//contacto.bvsalud.org/server.php?rqst=track&output=jcrpt&operator=morimarc&group=DeCSMeSH%20HighLighter&hg=Pw__&hcgs=MQ__&htgs=MQ__&hinv=MQ__&hfk=MQ__&nse="+Math.random();script.src=src;document.getElementById('lvztr_de9').appendChild(script);</script>
      <!-- livezilla.net PLACE SOMEWHERE IN BODY -->
   </body>
</html>
    """
  }
}
