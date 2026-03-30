window.dataLayer = window.dataLayer || [];
window.gtag = window.gtag || function () {
    window.dataLayer.push(arguments);
};
gtag("js", new Date());
gtag("config", "G-DBPY4Q6HT8");

function getDocumentLanguage() {
    var lang = (document.documentElement && document.documentElement.lang) || "";
    lang = (lang || "").trim().toLowerCase();

    if (lang === "pt" || lang === "en" || lang === "es" || lang === "fr") {
        return lang;
    }

    return "";
}

window.decsFinderPage = window.decsFinderPage || {
    originalInputText: "",
    language: getDocumentLanguage() || "en",
    inputLang: "",
    isFirstLoad: false,
    pendingTranslateRequest: null,
    translateSubmissionInProgress: false,
    translateButtonLocked: false
};

function getPageContext() {
    return window.decsFinderPage || {
        originalInputText: "",
        language: getDocumentLanguage() || "en",
        inputLang: "",
        isFirstLoad: false,
        pendingTranslateRequest: null,
        translateSubmissionInProgress: false,
        translateButtonLocked: false
    };
}

window.translateButtonObservers = window.translateButtonObservers || {
    inputAreaObserver: null,
    outputAreaObserver: null
};

function getNormalizedElementText(element) {
    if (!element) {
        return "";
    }

    var text = typeof element.innerText === "string" ? element.innerText : (element.textContent || "");
    return text.replace(/\u00A0/g, " ").trim();
}

function setTranslateSubmissionInProgress(isInProgress) {
    if (!window.decsFinderPage) {
        window.decsFinderPage = getPageContext();
    }

    window.decsFinderPage.translateSubmissionInProgress = !!isInProgress;
}

function isTranslateSubmissionInProgress() {
    return !!(window.decsFinderPage && window.decsFinderPage.translateSubmissionInProgress);
}

function setTranslateButtonLocked(isLocked) {
    if (!window.decsFinderPage) {
        window.decsFinderPage = getPageContext();
    }

    window.decsFinderPage.translateButtonLocked = !!isLocked;
}

function isTranslateButtonLocked() {
    return !!(window.decsFinderPage && window.decsFinderPage.translateButtonLocked);
}

function setTranslateButtonDisabled(isDisabled) {
    var translateButton = document.getElementById("translateButton");

    if (!translateButton) {
        return;
    }

    translateButton.disabled = !!isDisabled;
    translateButton.setAttribute("aria-disabled", isDisabled ? "true" : "false");
}

function updateTranslateButtonState() {
    var textWithTooltips = document.getElementById("textWithTooltips");

    if (isTranslateSubmissionInProgress() || isTranslateButtonLocked()) {
        setTranslateButtonDisabled(true);
        return;
    }

    var hasTextToTranslate = getNormalizedElementText(textWithTooltips).length > 0;
    setTranslateButtonDisabled(!hasTextToTranslate);
}

window.updateTranslateButtonState = updateTranslateButtonState;

function getInterfaceLanguage() {
    var context = getPageContext();
    return getDocumentLanguage() || context.language || "en";
}

window.handleTranslateButtonClick = function () {
    if (isTranslateSubmissionInProgress()) {
        return;
    }

    var textWithTooltips = document.getElementById("textWithTooltips");
    var hasTextToTranslate = getNormalizedElementText(textWithTooltips).length > 0;
    var inputTextLanguage = document.getElementById("inputTextLanguage");

    if (!hasTextToTranslate) {
        if (window.decsFinderPage) {
            window.decsFinderPage.pendingTranslateRequest = null;
        }
        setTranslateSubmissionInProgress(false);
        setTranslateButtonLocked(false);
        updateTranslateButtonState();
        return;
    }

    var interfaceLanguage = getInterfaceLanguage();
    var outputTextLanguage = document.getElementById("outputTextLanguage");
    var sendButton = document.getElementById("sendButton");
    var sourceLanguage = inputTextLanguage ? inputTextLanguage.value : "All languages";

    if (typeof gtag === "function") {
        gtag("event", "button_click", { "event_category": "button", "event_label": "Translate Button" });
    }

    if (window.decsFinderPage) {
        window.decsFinderPage.pendingTranslateRequest = {
            requested: true,
            sourceLanguage: sourceLanguage
        };
    }

    setTranslateSubmissionInProgress(true);
    setTranslateButtonLocked(true);
    setTranslateButtonDisabled(true);

    if (inputTextLanguage) {
        inputTextLanguage.value = interfaceLanguage;
    }
    if (outputTextLanguage) {
        outputTextLanguage.value = interfaceLanguage;
    }

    document.body.style.cursor = "wait";

    if (sendButton && typeof sendButton.click === "function") {
        sendButton.click();
        return;
    }

    submitPage(null, interfaceLanguage, "false");
};

function disconnectTranslateButtonObserver(observerKey) {
    var observer = window.translateButtonObservers[observerKey];

    if (!observer) {
        return;
    }

    observer.disconnect();
    window.translateButtonObservers[observerKey] = null;
}

function watchTranslateButtonDependency(element, observerKey) {
    disconnectTranslateButtonObserver(observerKey);

    if (!element || typeof MutationObserver !== "function") {
        return;
    }

    var observer = new MutationObserver(function () {
        updateTranslateButtonState();
    });

    observer.observe(element, {
        childList: true,
        subtree: true,
        characterData: true
    });

    window.translateButtonObservers[observerKey] = observer;
}

function initializeTranslateButtonState() {
    updateTranslateButtonState();
    watchTranslateButtonDependency(document.getElementById("textWithTooltips"), "inputAreaObserver");
    disconnectTranslateButtonObserver("outputAreaObserver");
    bindTranslateButtonInputEvents();
}

function bindTranslateButtonInputEvents() {
    var textWithTooltips = document.getElementById("textWithTooltips");

    if (!textWithTooltips || textWithTooltips.dataset.translateButtonBound === "true") {
        return;
    }

    textWithTooltips.dataset.translateButtonBound = "true";

    textWithTooltips.addEventListener("input", function () {
        setTranslateButtonLocked(false);
        setTranslateSubmissionInProgress(false);
        updateTranslateButtonState();
    });

    ["keyup", "paste", "cut", "drop"].forEach(function (eventName) {
        textWithTooltips.addEventListener(eventName, function () {
            window.setTimeout(function () {
                if (getNormalizedElementText(textWithTooltips).length > 0) {
                    setTranslateButtonLocked(false);
                    setTranslateSubmissionInProgress(false);
                }
                updateTranslateButtonState();
            }, 0);
        });
    });
}

function submitCurrentPage(showSR) {
    submitPage(null, getInterfaceLanguage(), showSR);
}

function clearTextAreas(language) {
    var inputTextLanguage = document.getElementById("inputTextLanguage");
    var outputTextLanguage = document.getElementById("outputTextLanguage");
    var textWithTooltips = document.getElementById("textWithTooltips");
    var textWithTooltipsAnnif = document.getElementById("textWithTooltipsAnnif");

    if (inputTextLanguage) {
        inputTextLanguage.value = "All languages";
    }
    if (outputTextLanguage) {
        outputTextLanguage.value = language;
    }
    if (textWithTooltips) {
        textWithTooltips.textContent = "";
        textWithTooltips.setAttribute("contenteditable", "true");
    }
    if (textWithTooltipsAnnif) {
        textWithTooltipsAnnif.textContent = "";
        textWithTooltipsAnnif.setAttribute("contenteditable", "true");
    }

    setTranslateButtonLocked(false);
    setTranslateSubmissionInProgress(false);
    updateTranslateButtonState();
    submitPage("", language, "false");
}

function submitPage(originalInputText, language, showSR) {
    var textWithTooltips = document.getElementById("textWithTooltips");
    var translateRequest = window.decsFinderPage && window.decsFinderPage.pendingTranslateRequest
        ? window.decsFinderPage.pendingTranslateRequest
        : null;

    if (window.decsFinderPage) {
        window.decsFinderPage.pendingTranslateRequest = null;
    }

    if (!textWithTooltips) {
        setTranslateButtonLocked(false);
        setTranslateSubmissionInProgress(false);
        return;
    }

    var inputText0 = textWithTooltips.innerHTML;
    var inputTextLen = inputText0.length;

    if (inputTextLen >= 230000) {
        var textWithTooltipsAnnif = document.getElementById("textWithTooltipsAnnif");
        var textMess = "";

        if (language === "es") {
            textMess = "El texto de entrada debe tener un máximo de 230000 caracteres.<br/>Intenta dividirlo en partes más pequeñas.";
        } else if (language === "pt") {
            textMess = "O texto de entrada deve ter no máximo 230000 caracteres.<br/>Experimente quebrar seu texto em textos menores.";
        } else if (language === "fr") {
            textMess = "Le texte saisi doit comporter au maximum 230000 caractères.<br/>Essayez de le diviser en sections plus courtes.";
        } else {
            textMess = "The input text must be a maximum of 230000 characters.<br/>Try breaking your text into smaller texts.";
        }

        textWithTooltips.textContent = textMess;
        if (textWithTooltipsAnnif) {
            textWithTooltipsAnnif.textContent = "";
            textWithTooltipsAnnif.setAttribute("contenteditable", "false");
        }
        textWithTooltips.setAttribute("contenteditable", "false");
        document.body.style.cursor = "default";
        setTranslateButtonLocked(false);
        setTranslateSubmissionInProgress(false);
        updateTranslateButtonState();
        return;
    }

    var originalInput = (originalInputText == null) ? getPageContext().originalInputText : originalInputText;
    var inputText = "";

    if (inputText0.includes("tooltip-link")) {
        inputText = originalInput.replaceAll("`", "'");
    } else {
        inputText = inputText0;
    }

    var inputLangElement = document.getElementById("inputTextLanguage");
    var outputLangElement = document.getElementById("outputTextLanguage");
    var inputLang = inputLangElement ? inputLangElement.value : "";
    var outputLang = outputLangElement ? outputLangElement.value : "";

    var trTypes = document.getElementById("termTypes");
    var termTypes = [];
    var i;

    if (trTypes) {
        for (i = 0; i < trTypes.options.length; i++) {
            if (trTypes.options[i].selected) {
                termTypes.push(trTypes.options[i].value);
            }
        }
    }

    var form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", "");
    form.acceptCharset = "UTF-8";

    appendHiddenField(form, "inputLang", inputLang);
    appendHiddenField(form, "outLang", outputLang);
    appendHiddenField(form, "inputText", inputText);
    if (trTypes && termTypes.length > 0) {
        appendHiddenField(form, "termTypes", termTypes.join("|"));
    }
    appendHiddenField(form, "lang", language);
    appendHiddenField(form, "isFirstLoad", "false");
    appendHiddenField(form, "showSR", showSR === "true" ? "true" : "false");
    if (translateRequest && translateRequest.requested) {
        appendHiddenField(form, "translateRequested", "true");
        appendHiddenField(form, "translateSourceLang", translateRequest.sourceLanguage || "All languages");
    }

    document.body.appendChild(form);
    form.submit();
}

function appendHiddenField(form, name, value) {
    var hiddenField = document.createElement("input");
    hiddenField.setAttribute("type", "hidden");
    hiddenField.setAttribute("name", name);
    hiddenField.setAttribute("value", value);
    form.appendChild(hiddenField);
}

function submitPageToSite(language) {
    var siteLanguage = language === "fr" ? "en" : language;
    var formS = document.createElement("form");
    formS.setAttribute("method", "post");
    formS.setAttribute("action", "https://decsfinderia.bvsalud.org/");

    appendHiddenField(formS, "lang", siteLanguage);

    document.body.appendChild(formS);
    formS.submit();
}

window.handleLanguageSelectionChange = function (selectEl, eventLabel) {
    if (!selectEl) {
        return;
    }

    document.body.style.cursor = "wait";

    if (typeof gtag === "function") {
        gtag("event", "language_selection", {
            event_category: "interaction",
            event_label: eventLabel || "Language selection",
            value: selectEl.value
        });
    } else {
        console.error("Google Analytics não está disponível.");
    }

    submitCurrentPage("false");
};

function exportTerms(exportText) {
    if (exportText != null && exportText.trim() !== "") {
        var now = new Date();
        var hours = now.getHours().toString().padStart(2, "0");
        var minutes = now.getMinutes().toString().padStart(2, "0");
        var seconds = now.getSeconds().toString().padStart(2, "0");
        var fileName = "DeCSFinder_" + hours + ":" + minutes + ":" + seconds;
        var blob = new Blob([exportText], { type: "text/plain;charset=utf-8" });
        saveAs(blob, fileName + ".txt");
    }
}

window.handleFChange = async function (event) {
    var context = getPageContext();
    var input = event.target;

    if (!input.files || input.files.length === 0) {
        document.body.style.cursor = "default";
        return;
    }

    var file = input.files[0];
    var isPdf = /pdf$/i.test(file.type) || /\.pdf$/i.test(file.name);
    var isText = /plain$/i.test(file.type) || /\.(txt|text)$/i.test(file.name);
    var text = "";

    if (isPdf) {
        try {
            var buffer = await file.arrayBuffer();
            var pdf = await pdfjsLib.getDocument({ data: buffer }).promise;

            for (var p = 1; p <= pdf.numPages; p++) {
                var page = await pdf.getPage(p);
                var content = await page.getTextContent();
                var pageText = content.items.map(function (item) { return item.str; }).join(" ");
                text += pageText + "\n";
            }
        } catch (err) {
            alert("Error reading PDF: " + err);
            document.body.style.cursor = "default";
            return;
        }
    } else if (isText) {
        try {
            text = await file.text();
        } catch (errText) {
            alert("Error reading text: " + errText);
            document.body.style.cursor = "default";
            return;
        }
    } else {
        alert("Only text and pdf files are allowed.");
        document.body.style.cursor = "default";
        return;
    }

    var textWithTooltips = document.getElementById("textWithTooltips");
    var inputTextLanguage = document.getElementById("inputTextLanguage");

    if (textWithTooltips) {
        textWithTooltips.textContent = text;
    }
    if (inputTextLanguage) {
        inputTextLanguage.value = "All languages";
    }

    submitCurrentPage("false");
};

function showDialog() {
    var dialog = document.getElementById("custom-dialog");
    if (dialog) {
        dialog.style.display = "flex";
    }
}

async function handleOpen() {
    var context = getPageContext();
    var urlInput = document.getElementById("url-input");
    var dialog = document.getElementById("custom-dialog");
    var url = urlInput ? urlInput.value : "";

    if (dialog) {
        dialog.style.display = "none";
    }

    if (!url || url.length === 0) {
        alert("empty url");
        return;
    }

    try {
        var conteudo = await fetchTextOrPDFContent(url);
        var textWithTooltips = document.getElementById("textWithTooltips");

        if (textWithTooltips) {
            textWithTooltips.textContent = conteudo.substring(0, 190000);
        }

        submitCurrentPage("false");
    } catch (err) {
        alert("Download error: " + err);
    }
}

function handleCancel() {
    var dialog = document.getElementById("custom-dialog");
    if (dialog) {
        dialog.style.display = "none";
    }
}

async function fetchTextOrPDFContent(targetUrl) {
    var proxyUrl = "https://corsproxy.io/?";
    var fullUrl = proxyUrl + encodeURIComponent(targetUrl);

    try {
        var response = await fetch(fullUrl, { method: "GET" });

        if (!response.ok) {
            console.error("Erro na resposta:", response);
            throw new Error("Erro ao acessar a URL: " + response.status + " " + response.statusText);
        }

        var contentType = response.headers.get("Content-Type") || "";

        if (contentType.includes("application/pdf") || targetUrl.endsWith(".pdf")) {
            return await extractTextFromPDF(response);
        }

        if (contentType.includes("text/plain") || contentType.includes("application/json")) {
            return await response.text();
        }

        alert("Formato não suportado [" + contentType + "]. Apenas arquivos texto ou PDF são aceitos.");
        throw new Error("Formato não suportado. Apenas arquivos texto ou PDF são aceitos.");
    } catch (error) {
        console.error("Erro ao buscar conteúdo:", error);
        return "";
    }
}

async function extractTextFromPDF(response) {
    try {
        var blob = await response.blob();
        var pdfData = await blob.arrayBuffer();
        var pdfjsModule = await import("https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.2.67/pdf.mjs");
        var loadingTask = pdfjsModule.getDocument({ data: pdfData });
        var pdf = await loadingTask.promise;
        var text = "";

        for (var i = 1; i <= pdf.numPages; i++) {
            var page = await pdf.getPage(i);
            var content = await page.getTextContent();
            var pageText = content.items.map(function (item) { return item.str; }).join(" ");
            text += "\n--- Página " + i + " ---\n" + pageText + "\n";
        }

        return text;
    } catch (error) {
        console.error("Erro ao processar PDF:", error);
        throw new Error("Não foi possível extrair texto do PDF.");
    }
}

function initializeSelectListeners() {
    var termTypes = document.getElementById("termTypes");

    if (termTypes) {
        termTypes.addEventListener("change", function () {
            var context = getPageContext();
            var selectedOptions = Array.from(this.selectedOptions).map(function (option) {
                return option.value;
            });

            if (typeof gtag === "function") {
                gtag("event", "type_terms_selection", {
                    event_category: "interaction",
                    event_label: selectedOptions.join(", "),
                    value: selectedOptions.length
                });
            } else {
                console.error("Google Analytics não está disponível.");
            }

            submitCurrentPage("false");
        });
    }
}

function initializeTooltips() {
    initializeCustomLinkTooltips();
    initializeBootstrapTooltips();
}

var activeTooltipState = window.activeTooltipState || {
    tooltipEl: null,
    targetEl: null
};
window.activeTooltipState = activeTooltipState;

function getTooltipHtml(element) {
    if (!element) {
        return "";
    }

    return element.getAttribute("data-tooltip-html")
        || element.getAttribute("data-original-title")
        || element.getAttribute("data-bs-title")
        || element.getAttribute("title")
        || "";
}

function ensureCustomTooltipElement() {
    var tooltipEl = document.getElementById("dmf-custom-tooltip");

    if (!tooltipEl) {
        tooltipEl = document.createElement("div");
        tooltipEl.id = "dmf-custom-tooltip";
        tooltipEl.className = "dmf-custom-tooltip";
        tooltipEl.setAttribute("role", "tooltip");
        tooltipEl.hidden = true;
        document.body.appendChild(tooltipEl);
    }

    return tooltipEl;
}

function positionCustomTooltip(tooltipEl, targetEl) {
    if (!tooltipEl || !targetEl) {
        return;
    }

    var spacing = 10;
    var rect = targetEl.getBoundingClientRect();
    var maxLeft = Math.max(8, window.innerWidth - tooltipEl.offsetWidth - 8);
    var left = Math.min(Math.max(8, rect.left + ((rect.width - tooltipEl.offsetWidth) / 2)), maxLeft);
    var top = rect.top - tooltipEl.offsetHeight - spacing;

    if (top < 8) {
        top = rect.bottom + spacing;
    }

    tooltipEl.style.left = left + "px";
    tooltipEl.style.top = top + "px";
}

function showCustomTooltip(targetEl) {
    var tooltipHtml = getTooltipHtml(targetEl);
    if (!tooltipHtml) {
        return;
    }

    var tooltipEl = ensureCustomTooltipElement();
    tooltipEl.innerHTML = tooltipHtml;
    tooltipEl.hidden = false;
    tooltipEl.style.visibility = "hidden";
    tooltipEl.style.display = "block";

    positionCustomTooltip(tooltipEl, targetEl);

    tooltipEl.style.visibility = "visible";
    activeTooltipState.tooltipEl = tooltipEl;
    activeTooltipState.targetEl = targetEl;
}

function hideCustomTooltip(targetEl) {
    if (!activeTooltipState.tooltipEl) {
        return;
    }

    if (targetEl && activeTooltipState.targetEl && activeTooltipState.targetEl !== targetEl) {
        return;
    }

    activeTooltipState.tooltipEl.hidden = true;
    activeTooltipState.tooltipEl.style.display = "none";
    activeTooltipState.tooltipEl.style.visibility = "hidden";
    activeTooltipState.tooltipEl.innerHTML = "";
    activeTooltipState.targetEl = null;
}

window.showDMFTooltip = function (event, targetEl) {
    var element = targetEl || (event && event.currentTarget) || (event && event.target);
    if (!element) {
        return;
    }

    showCustomTooltip(element);
};

window.hideDMFTooltip = function (event, targetEl) {
    var element = targetEl || (event && event.currentTarget) || (event && event.target);
    hideCustomTooltip(element);
};

function initializeCustomLinkTooltips() {
    var tooltipLinks = document.querySelectorAll(".tooltip-link");

    tooltipLinks.forEach(function (tooltipLinkEl) {
        if (tooltipLinkEl.dataset.tooltipInitialized === "true") {
            return;
        }

        var tooltipHtml = getTooltipHtml(tooltipLinkEl);
        if (!tooltipHtml) {
            return;
        }

        tooltipLinkEl.dataset.tooltipInitialized = "true";
        tooltipLinkEl.setAttribute("data-original-title", tooltipHtml);
        tooltipLinkEl.setAttribute("data-bs-title", tooltipHtml);
        tooltipLinkEl.setAttribute("title", "");

        tooltipLinkEl.addEventListener("mouseenter", function () {
            showCustomTooltip(tooltipLinkEl);
        });
        tooltipLinkEl.addEventListener("mouseleave", function () {
            hideCustomTooltip(tooltipLinkEl);
        });
        tooltipLinkEl.addEventListener("focus", function () {
            showCustomTooltip(tooltipLinkEl);
        });
        tooltipLinkEl.addEventListener("blur", function () {
            hideCustomTooltip(tooltipLinkEl);
        });
        tooltipLinkEl.addEventListener("click", function () {
            hideCustomTooltip(tooltipLinkEl);
        });
    });

    if (initializeCustomLinkTooltips.hasGlobalListeners) {
        return;
    }

    initializeCustomLinkTooltips.hasGlobalListeners = true;

    window.addEventListener("scroll", function () {
        if (activeTooltipState.tooltipEl && activeTooltipState.targetEl) {
            positionCustomTooltip(activeTooltipState.tooltipEl, activeTooltipState.targetEl);
        }
    }, true);

    window.addEventListener("resize", function () {
        if (activeTooltipState.tooltipEl && activeTooltipState.targetEl) {
            positionCustomTooltip(activeTooltipState.tooltipEl, activeTooltipState.targetEl);
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            hideCustomTooltip();
        }
    });
}

function initializeBootstrapTooltips() {
    var tooltipTriggerList = document.querySelectorAll("[data-bs-toggle=\"tooltip\"], [data-toggle=\"tooltip\"]");
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        if (tooltipTriggerEl.classList.contains("tooltip-link")) {
            return;
        }

        var tooltipHtml = getTooltipHtml(tooltipTriggerEl);
        if (!tooltipHtml) {
            return;
        }

        tooltipTriggerEl.setAttribute("data-original-title", tooltipHtml);
        tooltipTriggerEl.setAttribute("data-bs-title", tooltipHtml);
        tooltipTriggerEl.setAttribute("title", "");

        if (window.jQuery && window.jQuery.fn && typeof window.jQuery.fn.tooltip === "function") {
            window.jQuery(tooltipTriggerEl).tooltip({
                html: true
            });
            return;
        }

        if (window.bootstrap && typeof window.bootstrap.Tooltip === "function") {
            new window.bootstrap.Tooltip(tooltipTriggerEl, {
                html: true,
                title: tooltipHtml
            });
        }
    });
}

function initializePasteHandler() {
    var el = document.getElementById("textWithTooltips");
    if (!el) {
        return;
    }

    el.addEventListener("paste", function (e) {
        e.preventDefault();

        var dt = e.clipboardData || window.clipboardData;
        var text = dt ? (dt.getData("text/plain") || dt.getData("text") || "") : "";

        insertPlainText(el, text);
    });
}

function insertPlainText(el, text) {
    var tag = (el.tagName || "").toLowerCase();

    if (tag === "textarea" || tag === "input") {
        var start = (el.selectionStart == null) ? el.value.length : el.selectionStart;
        var end = (el.selectionEnd == null) ? el.value.length : el.selectionEnd;
        el.value = el.value.slice(0, start) + text + el.value.slice(end);
        var pos = start + text.length;
        el.setSelectionRange(pos, pos);
        return;
    }

    if (el.isContentEditable) {
        if (document.queryCommandSupported && document.queryCommandSupported("insertText")) {
            document.execCommand("insertText", false, text);
        } else {
            var sel = window.getSelection();
            if (!sel || sel.rangeCount === 0) {
                return;
            }
            sel.deleteFromDocument();
            sel.getRangeAt(0).insertNode(document.createTextNode(text));
            sel.collapseToEnd();
        }
        return;
    }

    el.textContent = (el.textContent || "") + text;
}

function initializeDialogActions() {
    var fileChooser = document.getElementById("fileChooser");
    var openButton = document.getElementById("open-btn");
    var cancelButton = document.getElementById("cancel-btn");

    if (fileChooser) {
        fileChooser.onchange = window.handleFChange;
    }
    if (openButton) {
        openButton.onclick = handleOpen;
    }
    if (cancelButton) {
        cancelButton.onclick = handleCancel;
    }
}

function initializeFirstLoadModal() {
    if (!getPageContext().isFirstLoad) {
        return;
    }

    if (window.$ && typeof window.$ === "function" && document.getElementById("myModal")) {
        window.$("#myModal").modal("show");
    }

    var closeButton = document.querySelector(".close");
    if (closeButton) {
        closeButton.addEventListener("click", function () {
            if (window.$ && typeof window.$ === "function" && document.getElementById("myModal")) {
                window.$("#myModal").modal("hide");
            }
        });
    }
}

function initializeDeCSFinderPageRuntime() {
    initializeTooltips();
    initializeDialogActions();
    initializeSelectListeners();
    initializePasteHandler();
    initializeFirstLoadModal();
    initializeTranslateButtonState();
}

window.initializeDeCSFinderPage = function (config) {
    window.decsFinderPage = {
        originalInputText: (config && config.originalInputText) || "",
        language: (config && config.language) || "en",
        inputLang: (config && config.inputLang) || "",
        isFirstLoad: !!(config && config.isFirstLoad),
        pendingTranslateRequest: null,
        translateSubmissionInProgress: false,
        translateButtonLocked: !!(config && config.translateButtonLocked)
    };

    initializeDeCSFinderPageRuntime();
};

function handleXXX3(event) {
    return event;
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
        initializeTooltips();
    });
} else {
    initializeTooltips();
}
