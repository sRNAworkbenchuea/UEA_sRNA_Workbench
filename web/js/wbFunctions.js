// A place for commonly used functions across graphing scripts

// recursively finds all unique values of the key
// by diving down through each array with each key in array
// returns array of the unique values of key
function findAll(obj, arrays, key)
{
    // recursively dives down through a json hierachy
    var refind = function (obj, arrays)
    {
        var thisarr = [];
        var thisArray = arrays.slice(0);
        var a = thisArray.shift();
        if (thisArray.length > 0)
        {
            // recurse condition: more levels in key array
            // recursively call refind for the next level of elements
            obj.forEach(function (x) {
                var thisobj = x[a];
                thisarr = thisarr.concat(refind(thisobj, thisArray));
            });
        }
        else
        {
            // stop condition: nothing else in key array
            // access this key and concat the next level of arrays together
            obj.forEach(function (x) {
                thisarr = thisarr.concat(x[a]);
            });
        }
        return thisarr;
    };

    // This will bring up a non-unique array of elements
    var allObj = refind(obj, arrays);
    
    // Turn array into unique list of elements
    var uObj = {};
    allObj.forEach(function (x)
    {
        uObj[x[key]] = 1;
    });
    
    // return unique array of elements
    return Object.keys(uObj);
}

function findUnique(array, key)
{
    // Turn array into unique list of elements
    var uObj = {};
    array.forEach(function (x)
    {
        uObj[x[key]] = 1;
    });
    
    // return unique array of elements
    return Object.keys(uObj);
}


// Shorthand for writing out a translate function as a string
// used in d3.selection.attr("transform", )
function translateStr(tx, ty)
{
    return "translate(" + tx + "," + ty + ")";
}

// Find array element which has a key value of val 
function find(arr, key, val) { 
    for (var ai, i = arr.length; i--; )
        if ((ai = arr[i]) && ai[key] == val)
            return ai;
    return null;
}

function findAll2(arr, key, val) { 
    var toReturn = [];
    for (var ai, i = arr.length; i--; )
        if ((ai = arr[i]) && ai[key] == val)
            toReturn.push(ai);
    return toReturn;
}

//return an array of values that match on a certain key
function getValues(obj, key) {
    var objects = [];
    for (var i in obj) {
        if (!obj.hasOwnProperty(i)) continue;
        if (typeof obj[i] == 'object') {
            objects = objects.concat(getValues(obj[i], key));
        } else if (i == key) {
            objects.push(obj[i]);
        }
    }
    return objects;
}

