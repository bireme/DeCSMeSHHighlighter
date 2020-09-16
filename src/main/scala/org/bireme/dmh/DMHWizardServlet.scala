/*=========================================================================

    DeCSMeSHHighlighter © Pan American Health Organization, 2020.
    See License at: https://github.com/bireme/DeCSMeSHHighlighter/blob/master/LICENSE.txt

  ==========================================================================*/

package org.bireme.dmh

import java.io.{InputStream, PrintWriter}

import javax.servlet.{ServletConfig, ServletContext}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import scala.util.{Failure, Success, Try}

/**
  * DeCSMeSHHighlighter Wizard Servlet
  */
class DMHWizardServlet extends HttpServlet {
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

    println("DMHWizardServlet is listening ...")
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
    <meta name="autor" content=" BIREME | OPAS | OMS - > Márcio Alves">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DeCS/MeSH Highlighter - Wizard</title>
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css">
    <link rel="stylesheet" href="wizardDeCSH/css/bootstrap.min.css">
    <link rel="stylesheet" href="wizardDeCSH/css/wizard.css">
    <link rel="stylesheet" href="wizardDeCSH/css/accessibility.css">
    <link rel="stylesheet" href="wizardDeCSH/css/style.css">
    <link rel="shortcut icon" href="wizardDeCSH/img/favicon.png">
</head>
<body onload="initialClean()">
<script type="text/javascript">

    function initialClean() {
        //alert("entrando no body")
        document.getElementById("text1").value = "";
        document.getElementById("select1").value = "";
        document.getElementById("select2").value = "";
        document.getElementById('main_headings').checked = true;
        document.getElementById('qualifiers').checked = true;
        document.getElementById('entry_terms').checked = true;
        document.getElementById('publication_types').checked = true;
        document.getElementById('check_tags').checked = true;
        document.getElementById('geographics').checked = true;
        document.getElementById('geographics').checked = true;
    }

    function submitPage(plang, useAllTermTypes, leaveWizard) {
//alert("Entrando no submitPage() useAllTermTypes=[" + useAllTermTypes + "]");
        var inputText = document.getElementById("text1").value;
        var inputLang = document.getElementById("select1").value;
        var outputLang = document.getElementById("select2").value;

        var pageLang = """" + language + """";
        var language;
        if (plang === "") language = pageLang;
        else language = plang;

        var form = document.createElement("form");
        form.setAttribute("method", "post");

        if (leaveWizard) form.setAttribute("action", "dmh");
        else form.setAttribute("action", "dmhw");

        var hiddenField1 = document.createElement("input");
        hiddenField1.setAttribute("type", "hidden");
        hiddenField1.setAttribute("name", "inputLang");
        hiddenField1.setAttribute("value", inputLang);
        form.appendChild(hiddenField1);

        var hiddenField2 = document.createElement("input");
        hiddenField2.setAttribute("type", "hidden");
        hiddenField2.setAttribute("name", "outLang");
        hiddenField2.setAttribute("value", outputLang);
        form.appendChild(hiddenField2);

        var hiddenField3 = document.createElement("input");
        hiddenField3.setAttribute("type", "hidden");
        hiddenField3.setAttribute("name", "inputText");
        hiddenField3.setAttribute("value", inputText);
        form.appendChild(hiddenField3);

        var termTypesStr = "";
        if (useAllTermTypes) {
            termTypesStr = termTypesStr + "Main headings|Qualifiers|Entry terms|Publication types|Check tags|Geographics";
        } else {
            if (document.getElementById('main_headings').checked) {
                termTypesStr = termTypesStr + "Main headings"
            }
            if (document.getElementById('qualifiers').checked) {
                if (termTypesStr.length > 0) {
                    termTypesStr = termTypesStr + "|"
                }
                termTypesStr = termTypesStr + "Qualifiers"
            }
            if (document.getElementById('entry_terms').checked) {
                if (termTypesStr.length > 0) {
                    termTypesStr = termTypesStr + "|"
                }
                termTypesStr = termTypesStr + "Entry terms"
            }
            if (document.getElementById('publication_types').checked) {
                if (termTypesStr.length > 0) {
                    termTypesStr = termTypesStr + "|"
                }
                termTypesStr = termTypesStr + "Publication types"
            }
            if (document.getElementById('check_tags').checked) {
                if (termTypesStr.length > 0) {
                    termTypesStr = termTypesStr + "|"
                }
                termTypesStr = termTypesStr + "Check tags"
            }
            if (document.getElementById('geographics').checked) {
                if (termTypesStr.length > 0) {
                    termTypesStr = termTypesStr + "|"
                }
                termTypesStr = termTypesStr + "Geographics"
            }
            if (termTypesStr.length == 0) {
                termTypesStr = termTypesStr + "Main headings"
            }
        }
//alert("TermTypesStr=" + termTypesStr);
        var hiddenField4 = document.createElement("input");
	      hiddenField4.setAttribute("type", "hidden");
	      hiddenField4.setAttribute("name", "termTypes");
	      hiddenField4.setAttribute("value", termTypesStr);
	      form.appendChild(hiddenField4);

        var hiddenField5 = document.createElement("input");
	      hiddenField5.setAttribute("type", "hidden");
	      hiddenField5.setAttribute("name", "lang");
	      hiddenField5.setAttribute("value", language);
	      form.appendChild(hiddenField5);

        document.body.appendChild(form);

        form.submit();
    }

    function submitPageToSite(plang) {
        var pageLang = """" + language + """";
        var language;
        if (plang === "") language = pageLang;
        else language = plang;

        var formS = document.createElement("form");
        formS.setAttribute("method", "post");
        formS.setAttribute("action", "dmhs");

        var hiddenFieldLang = document.createElement("input");
        hiddenFieldLang.setAttribute("type", "hidden");
        hiddenFieldLang.setAttribute("name", "lang");
        hiddenFieldLang.setAttribute("value", language);
        formS.appendChild(hiddenFieldLang);

        document.body.appendChild(formS);

        formS.submit();
     }
</script>

<section id="barAccessibility">
    <div class="container">
        <div class="row">
            <div class="col-md-6" id="accessibilityTutorial">
               <!--
               <a href="#main_container" tabindex="1" role="button">""" + i18n.translate("Main Content", language) + """ <span class="hiddenMobile">1</span></a>
               <a href="#nav" tabindex="2" role="button">Menu <span class="hiddenMobile">2</span></a>
               <a href="#fieldSearch" tabindex="3" id="accessibilitySearch" role="button">""" + i18n.translate("Search", language) + """ <span class="hiddenMobile">3</span></a>
               <a href="#footer" tabindex="4" role="button">""" + i18n.translate("Footer", language) + """ <span class="hiddenMobile">4</span></a>
                -->
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
				    <div class="col-12" id="logoDeCS">
					      <a href="javascript:submitPageToSite('""" + language + """');"><img src="wizardDeCSH/img/logo.svg" alt="" class="imgBlack"></a>
				    </div>
		    </div>
		</div>
</header>
<div class="container">
    <div class="row">
        <div class="col-md-4">
            <h3 class="title">""" + i18n.translate("Choose one of the options below to identify the DeCS/MeSH terms in your text", language) + """.</h3>
            <div class="btn-group">
                <a href="#modalWizard" data-toggle="modal" data-target=".bd-example-modal-lg" class="btn btn-lg btn-success">""" + i18n.translate("SIMPLE", language) + """</a>
                <a href="#" class="btn btn-lg btn-outline-success" onclick='submitPage("""" + language + """", false, true)'>""" + i18n.translate("ADVANCED", language) + """</a>
            </div>
        </div>
        <div class="col-md-8">
            <img src="wizardDeCSH/img/telas.png" class="img-fluid" alt="">
        </div>
    </div>
</div>
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
<div class="modal fade bd-example-modal-lg " tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header wizardModalTitle">
                <h4 class="text-center" >""" + i18n.translate("Choose one of the options below to identify the DeCS/MeSH terms in your text", language) + """. <br> <small>""" + i18n.translate("Answer the following questions", language) + """:</small></h4>
            </div>
            <!-- Etapa1 -->
            <div class="modal-body wizardModalBody" id="etapa1">
                <div>
                    <ul id="wizard">
                        <li><i class="fas fa-circle icone3"></i><i class="fas fa-chevron-right icone"></i></li>
                        <li><i class="fas fa-circle icone2"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("What is the language of your text", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <select name="" id="select1" class="form-control wizardColor">
                            <option value="" disabled="" selected data-toggle="collapse" data-target="#etapa2">""" + i18n.translate("Select", language) + """</option>
                            <option value="All Languages">""" + i18n.translate("I don't know", language) + """</option>
                            <option value="en">""" + i18n.translate("English", language) + """</option>
                            <option value="es">""" + i18n.translate("Spanish", language) + """</option>
                            <option value="pt">""" + i18n.translate("Portuguese", language) + """</option>
                            <option value="fr">""" + i18n.translate("French", language) + """</option>
                        </select>
                    </div>
                </div>
            </div>
            <!-- Etapa 2 -->
            <div class="modal-body wizardModalBody collapse" id="etapa2">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone3"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("Paste your text below", language) + """</h5>
                    </div>
                    <div class="col-12 marginTB1">
                        <textarea name="" id="text1" cols="30" rows="10" class="form-control"></textarea>
                    </div>
                    <div class="col-12 marginTB1">
                        <button type="button" class="btn btn-success" id="btEtapa2">""" + i18n.translate("Next", language) + """</button>
                    </div>
                </div>
            </div>
            <!-- Etapa3 -->
            <div class="modal-body wizardModalBody collapse" id="etapa3">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc2">""" + i18n.translate("Paste your text below", language) + """</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone3"></i><i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone2"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("In what language do you want to see the DeCS/MeSH terms", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <select name="" id="select2" class="form-control wizardColor2">
                            <option value="" disabled="" selected>""" + i18n.translate("Select", language) + """</option>
                            <option value="All Languages">""" + i18n.translate("The same found in the text", language) + """</option>
                            <option value="en">""" + i18n.translate("English", language) + """</option>
                            <option value="es">""" + i18n.translate("Spanish", language) + """</option>
                            <option value="pt">""" + i18n.translate("Portuguese", language) + """</option>
                            <option value="fr">""" + i18n.translate("French", language) + """</option>
                        </select>
                    </div>
                </div>
            </div>
            <!-- etapa 4 -->
            <div class="modal-body wizardModalBody collapse" id="etapa4">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc2">""" + i18n.translate("Paste your text below", language) + """</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc3">""" + i18n.translate("In what language do you want to see the DeCS/MeSH terms", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><i class="fas fa-circle icone3"></i></li>
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("Do you want to use advanced filters", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <div class="row marginTB1">
                            <div class="col-md-6 marginTB1">
                                <div class="card h-100 cartbt wizardColor3" id="btEtapa4b">
                                    <a href="#" style="text-decoration: none;" onclick='submitPage("""" + language + """", true, true)'>
                                        <div class="card-body">
                                          """ + i18n.translate("No, proceed with the identification of DeCS/MeSH terms", language) + """
                                        </div>
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="card h-100 cartbt wizardColor3" id="btEtapa4">
                                    <div class="card-body">
                                      """ + i18n.translate("Yes, go to them", language) + """
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- etapa 5 -->
            <div class="modal-body wizardModalBody collapse" id="etapa5">
                <div>
                    <ul id="wizard">
                        <li><a href="#!" class="bc1">""" + i18n.translate("What is the language of your text", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc2">""" + i18n.translate("Paste your text below", language) + """</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc3">""" + i18n.translate("In what language do you want to see the DeCS/MeSH terms", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li>
                        <li><a href="#!" class="bc4">""" + i18n.translate("Do you want to use advanced filters", language) + """?</a></li>
                        <!--li><a href="#!" class="bc4">""" + i18n.translate("Which types of terms do you want to identify", language) + """?</a> <i class="fas fa-chevron-right icone "></i></li-->
                    </ul>
                </div>
                <div id="e1" class="row marginTB1">
                    <div class="col-md-12">
                        <h5 class="text-center">""" + i18n.translate("Which types of terms do you want to identify", language) + """?</h5>
                    </div>
                    <div class="col-12">
                        <div class="row marginTB1">
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="main_headings">
                                    <label class="custom-control-label" for="main_headings">""" + i18n.translate("Main headings", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="qualifiers">
                                    <label class="custom-control-label" for="qualifiers">""" + i18n.translate("Qualifiers", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="entry_terms">
                                    <label class="custom-control-label" for="entry_terms">""" + i18n.translate("Entry terms", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="publication_types">
                                    <label class="custom-control-label" for="publication_types">""" + i18n.translate("Publication types", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="check_tags">
                                    <label class="custom-control-label" for="check_tags">""" + i18n.translate("Check tags", language) + """</label>
                                </div>
                            </div>
                            <div class="col-md-6 marginTB1">
                                <div class="custom-control custom-checkbox">
                                    <input type="checkbox" class="custom-control-input" id="geographics">
                                    <label class="custom-control-label" for="geographics">""" + i18n.translate("Geographics", language) + """</label>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <a href="#" class="btn btn-success" onclick='submitPage("""" + language + """", false, true)'>""" + i18n.translate("Search", language) + """</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="wizardDeCSH/js/jquery-3.3.1.min.js"></script>
<script src="wizardDeCSH/js/bootstrap.min.js"></script>
<script src="wizardDeCSH/js/cookie.js"></script>
<script src="wizardDeCSH/js/accessibility.js"></script>
<script src="wizardDeCSH/js/wizard.js"></script>
</body>
</html>
    """
  }
}
