/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function showDiv(idInfo) {
    var sel = document.getElementById('plotsection').getElementsByTagName('div');
    for (var i = 0; i < sel.length; i++) {
        sel[i].style.display = 'none';
    }
    for (var i = 0; i < idInfo.length; i++)
    {
        showSingleDiv(idInfo[i]);
    }

}
function hideSingleDiv(id)
{
    document.getElementById(id).style.display = 'none';
}
function showSingleDiv(id)
{
    document.getElementById(id).style.display = 'block';
}

function returnToMain()
{
    app.returnToMainWorkflow();
}

// @author chris
// function to add html content (val) to element with specified id
function setInnerHTML(id, val)
{
    var element = document.getElementById(id);
    if (element != null)
    {
        element.innerHTML = val;
    }
}
/*
 * File creation
 */
var getFileBlob = function (url, cb)
{
    var xhr = new XMLHttpRequest();
    xhr.open("GET", url);
    xhr.responseType = "blob";
    xhr.addEventListener('load', function () {
        cb(xhr.response);
    });
    xhr.send();
};

var blobToFile = function (blob, name)
{
    blob.lastModifiedDate = new Date();
    blob.name = name;
    return blob;
};

var getFileObject = function (filePathOrUrl, cb)
{
    getFileBlob(filePathOrUrl, function (blob) {
        cb(blobToFile(blob, 'test.jpg'));
    });
};

            