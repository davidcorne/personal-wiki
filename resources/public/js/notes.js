"use strict";

//=============================================================================
var notes = {
}

//=============================================================================
notes.setHtml = function(text) {
    var notesElements = document.getElementsByClassName('note-body');
    notesElements[notesElements.length -1].innerHTML = notes.transform(text);
}


//=============================================================================
notes.transform = function(text) {
    var reader = new commonmark.Parser();
    var writer = new commonmark.HtmlRenderer();
     // parsed is a 'Node' tree
    var parsed = reader.parse(text);
    var result = writer.render(parsed);
    return result;
}
