// Sets up a global options object that stores common settings for charts.
// 
// Contains functions to update these options from the main view and to act on
// these options frmo the charts.

var options = {};


options.availableBpWindows = [];

options.setBpWindows = function(windows)
{
    //console.log(windows);
    options.availableBpWindows = windows;
    options.bpWindow = windows[0];
};

// Update the currently selected options. This should be called
// wheneve an option changes in the main view
options.updateOptions = function ()
{
    
    options.selectedSizes = localStorage.slectedSizes;
    

    // grouping settings
    var gb = localStorage.groupBy;
    if (gb === "Plot")
        options.groupBy = ["Plot", "Normalisation"];
    else
        options.groupBy = ["Normalisation", "Plot"];

    // If this is undefined, the plots should be produced with the first offset
    options.offset = localStorage.maOffset;
    options.dataType = localStorage.maDataType;
    
    // boxplot window setting
    options.bpWindow = localStorage.bpWindow;
    
    options.bpShowNBar = localStorage.bpShowNBar;
    options.bpShowLegend = localStorage.bpShowLegend;
    options.bpShowLabels = localStorage.bpShowLabels;
    
    options.sizeClassDataType = "Redundant";
    if(localStorage.COMP === "true"){
        options.sizeClassDataType = "Complexity";
    }
    else if(localStorage.NR === "true" ){
        options.sizeClassDataType = "Nonredundant";
    }
    
    options.updateAnnotationOptions();
    options.updateNormalisationOptions();
    
};

// update annotation options using a node list of checkboxes
options.updateAnnotationOptions = function()
{
    options.annotationTypes = [];
        // Go through checkboxes and store checked ones in options object
    var annotChecks = document.getElementsByName("annot_radio");
    
    options.annotationTypes = [];
    for(i=0; i<annotChecks.length; i++)
    {
        var type = annotChecks[i].id;
        if(annotChecks[i].checked === true)
            options.annotationTypes.push(type);
        
    }
    //console.log(annotChecks);
};

// update annotation options using a node list of checkboxes
options.updateNormalisationOptions = function()
{
    options.normTypes = [];
        // Go through checkboxes and store checked ones in options object
    var normChecks = document.getElementsByName("norm_radio");
    
    //console.log(normChecks);
    
    options.normTypes = [];
    for(i=0; i<normChecks.length; i++)
    {
        var type = normChecks[i].id;
        //console.log(type);
        if(normChecks[i].checked === true)
        {
            //console.log("checked");
            options.normTypes.push(type);
        }
        else
        {
            //console.log("unchecked");
        }
    }
    //console.log(options.normTypes);
};

options.filterNormArray = function(norms, key){
    if(key === undefined)
        key = "Normalisation";
    var filtered = [];
    options.normToShow.forEach(function (k)
    {
        norms.forEach(function (n)
        {
            //var found = n[key] === options.normKeys[k];
            var found = n[key] === k;
            if (found)
            {
                filtered.push(n);
            }
        });
    });
    return filtered;
};


// Filter a generic array and return just the elements containing
// just stuff from values array held in a key
options.filterArray = function(array, key, values)
{
    var filtered = [];
    values.forEach(function(d)
    {
        var found = find(array, key, d);
        if(found !== null)
        {
            filtered.push(found);
        }
    });
    return filtered;
}

options.filterArray2 = function(array, key, values)
{
    var filtered = [];
    values.forEach(function(d)
    {
        array.forEach(function(a){
            if(a[key] === d)
            {
                filtered.push(a);
            }
        })
    });
    return filtered;
}


options.filterSizes = function(sizes)
{
    return options.filterArray(sizes, "key", options.selectedSizes);
}

