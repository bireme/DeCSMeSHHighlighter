function handleXXX2(event) {
    alert("entrando no handleXXX2" + event);
}

function clearTextAreas(language) {
    //alert("entrando no clearTextAreas");

    var inputTextLanguage = document.getElementById("inputTextLanguage");
    var outputTextLanguage = document.getElementById("outputTextLanguage");
    var textWithTooltips = document.getElementById("textWithTooltips");
    var superResumos = document.getElementById("superResumos");
    var textWithTooltipsAnnif = document.getElementById("textWithTooltipsAnnif");

    inputTextLanguage.value = "All languages";
    outputTextLanguage.value = language;
	textWithTooltips.textContent = "";
    textWithTooltips.setAttribute("contenteditable", "true");
    textWithTooltipsAnnif.textContent = "";
    textWithTooltipsAnnif.setAttribute("contenteditable", "true");
    superResumos.textContent = "";
    submitPage(``, language, "false");
}

function submitPage(originalInputText, language, showSR) {
    //alert("Entrando no submitPage() originalInputText=" + originalInputText + " language=" + language + " showSR=" + showSR);
    //alert("originalInputText=[""" + originalInputText + """]");
    var inputText0 = document.getElementById('textWithTooltips').innerHTML;
    //alert("inputText0=[" + inputText0 + "]");

    var inputText= "";
    if (inputText0.includes("tooltip-link")) {
        inputText = originalInputText.replaceAll("`", "'") ;
    } else {
        inputText = inputText0;
    }
    //alert("inputText=[" + inputText + "]");

    var inputLang = document.getElementById("inputTextLanguage").value;
    var outputLang = document.getElementById("outputTextLanguage").value;

    var trTypes = document.getElementById("termTypes");
	var termTypes = new Array();
    var i;
    var count = 0;

    for (i = 0; i < trTypes.options.length; i++) {
        if (trTypes.options[i].selected) {
            termTypes[count] = trTypes.options[i].value;
            count = count + 1
        }
    }

    var form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", "dmf");
    form.acceptCharset = "UTF-8";

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
	for (i = 0; i < termTypes.length; i++) {
        if (i > 0) {
            termTypesStr = termTypesStr + "|";
        }
        termTypesStr = termTypesStr + termTypes[i];
    }

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

    var hiddenField6 = document.createElement("input");
    hiddenField6.setAttribute("type", "hidden");
    hiddenField6.setAttribute("name", "isFirstLoad");
    hiddenField6.setAttribute("value", "false");
    form.appendChild(hiddenField6);

    var showSuperResumos = (showSR === "true") ? "true" : "false";
    var hiddenField7 = document.createElement("input");
    hiddenField7.setAttribute("type", "hidden");
    hiddenField7.setAttribute("name", "showSR");
    hiddenField7.setAttribute("value", showSuperResumos);
    form.appendChild(hiddenField7);

    document.body.appendChild(form);

    form.submit();
}

function submitPageToSite(language) {
    var formS = document.createElement("form");
    formS.setAttribute("method", "post");
    formS.setAttribute("action", "dmfs");

    var hiddenFieldLang = document.createElement("input");
    hiddenFieldLang.setAttribute("type", "hidden");
    hiddenFieldLang.setAttribute("name", "lang");
    hiddenFieldLang.setAttribute("value", language);
    formS.appendChild(hiddenFieldLang);

    document.body.appendChild(formS);

    formS.submit();
}

function exportTerms(exportText) {
    //alert("entrando no exportTerms");
    if (exportText != null && exportText.trim() !== "")  {
        var now = new Date();
        var hours = now.getHours().toString().padStart(2, '0');
        var minutes = now.getMinutes().toString().padStart(2, '0');
        var seconds = now.getSeconds().toString().padStart(2, '0');
        var fileName = `DeCSFinder_${hours}:${minutes}:${seconds}`;
        var blob = new Blob([exportText], { type: "text/plain;charset=utf-8" });
        saveAs(blob, fileName + ".txt");
    }
}

function handleXXX3(event) {
 //alert("entrando no handleXXX2");
 //alert("aqui!");
  /*var input = event.target;
  if (!input.files || input.files.length === 0) { return; }

  var file   = input.files[0];
  var isPdf  = /pdf$/i.test(file.type) || /\.pdf$/i.test(file.name);
  var text   = "";

  //alert ("isPdf=" + isPdf)
  if (isPdf) {
    // lê como ArrayBuffer → PDF.js
    const buffer = await file.arrayBuffer();
    const pdf    = await pdfjsLib.getDocument({ data: buffer }).promise;

    for (let p = 1; p <= pdf.numPages; p++) {
      var page   = await pdf.getPage(p);
      var cont   = await page.getTextContent();
      text       += cont.items.map(it => it.str).join(" ") + "\n";
    }
  } else {
    // TXT simples
    text = await file.text();
  }

  //alert("text=" + text);*/
  // envia para o backend (ou processe localmente)
  //submitPage(text, "") /* segundo arg. = language se precisar */
}
