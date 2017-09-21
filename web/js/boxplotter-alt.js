boxplot = {};

(function () {
    var DDSlider;
    var boxplotWindows;
    boxplot.margins = {"left": 40, "right": 40, "top": 40, "bottom": 40};
    boxplot.dim = {"width": 800, "height": 250};
    boxplot.titlePadding = 40;
    // TODO: Dynamically window with max height of nice looking
    // graph and facet into min height before requiring to scroll
    
    var bpOptions = {
        maxGraphWidth: boxplot.dim.width,
        maxBoxWidth: 30,
        graphHeight: 150,
        spacing: 0.25,
        transitionDuration: 400
    };
    var sizeHeight = 250;
    var padding = 40;
    var titlePadding = 10;
    var yAxisPadding = 15; // padding to leave room for y axis

    var maxBoxWidth = 30; // boxplot.dim.height/10;
    var spacing = 0.25;

    var transitionDuration = 400;
      
    boxplot.updateSlider = function()
    {
        if (DDSlider !== undefined)
        {
            var bp_step = DDSlider.getStep();
            localStorage.bpWindow = options.availableBpWindows[bp_step[0]-1];
            options.updateOptions();
        }

        
    };
    
    boxplot.initialise = function ()
    {
        
        if(!d3.select(".boxplot_svg").empty())
            d3.select(".boxplot_svg").remove();
        // check that boxplot isn't initialised already
        boxplot.svg = d3.select("#aboxplot").append("svg").attr("class", "boxplot_svg")
                //.attr("viewBox", "0 0 800 400")
                .attr("height", boxplot.dim.height + boxplot.margins.top + boxplot.margins.bottom+ boxplot.titlePadding)
                .attr("width", boxplot.dim.width + boxplot.margins.left + boxplot.margins.right);
        boxplot.svg = boxplot.svg
                .append("g")
                .attr("transform", "translate(" + boxplot.margins.left + "," + (boxplot.margins.top + boxplot.titlePadding) + ")");
        
        DDSlider = new Dragdealer('bp_window-slider', {
            steps: options.availableBpWindows.length,
            speed: 1,
            animationCallback: function (x, y) {

                boxplot.updateSlider();
                $('#bp_window-slider .value').text(options.bpWindow);

                //options.updateOptions();
            },
            callback: function (x, y) {
                d3.json(getDataLocation(JSON_AD), function (error, json)
                {
                    boxplot.build(json);
                });
            }
        });
    };

    boxplot.build = function (json)
    {



        //var norms = json.LogBases[0].Normalisations;
        var norms = options.filterNormArray(json);

        // Nest data
        var byAnnotation = d3.nest()
                .key(function(x){return x.Annotation})
                .key(function(x){return x.Window})
                .key(function(x){return x.Normalisation})
                .entries(norms);

        var annotFiltered = options.filterArray(byAnnotation, "key", options.annotationTypes);
        
        // resize svg height to fit all graphs
        console.log((annotFiltered.length*sizeHeight) + boxplot.margins.top + boxplot.margins.bottom+ boxplot.titlePadding);
        boxplot.svg.attr("height", (annotFiltered.length*sizeHeight) + boxplot.margins.top + boxplot.margins.bottom+ boxplot.titlePadding);
  
        // colour scale
        var byFilename = d3.nest().key(function(x){return x.Filename}).entries(norms);
        var colours = d3.scale.category10().domain(byFilename.map(function(f){ return f.key; })); 
        
        // find all windows used and sort by top
        var thisWindow;
        thisWindow = options.bpWindow;

        // If no window is given by options, select the window
        // that defines the most abundant sequences.
        if(thisWindow === 'undefined' || thisWindow === undefined)
        {
            var windows = json.map(function(x){return x.Window;});
            windows.sort(function (a, b)
            {
                var topCountRe = /([\d.]+)$/;
                var acount = topCountRe.exec(a)[1];
                var bcount = topCountRe.exec(b)[1];
                if (acount < bcount)
                    return 1;
                if (acount > bcount)
                    return -1;
                return 0;
            });
            thisWindow = windows[0];
        }
        
        var showLabels = (options.bpShowLabels === "true") ? [true] : [];
        var showLegend = (options.bpShowLegend === "true") ? [true] : [];
        var showN = (options.bpShowNBar === "true") ? [true] : [];
        var bpObject = new Boxplot(thisWindow, bpOptions, 
                                    showLabels, showLegend, 
                                    showN);


        // Add the legend for the window being used above the plot
//        var winTitle = boxplot.svg.selectAll("text.window.title").data([1], function(d){return d;});
//        winTitle.enter().append("text")
//                .attr("class","window title")
//                .attr("x", boxplot.margins.left)
//                .attr("y", boxplot.titlePadding)
//                .text("Window: "+thisWindow);
//
//        // window update
//        winTitle.text("Window: "+thisWindow);

        // Selection over annotation facets
        var graphSelect = boxplot.svgg.selectAll("g.graph").data(annotFiltered, function (d) {return d.key;});

        // Facet update
        graphSelect.transition().attr("transform", function(d,i){
            return translateStr(0, i*(sizeHeight+padding + titlePadding));
        }).each(function(d){
            
            // find the window currently in use
            var norms = find(d.values, "key", thisWindow).values;
            var N = getN(thisWindow, sizeHeight);
            var thisGraph = d3.select(this);
            bpObject.addBoxplotGroup(thisGraph, norms, "Filename", N);

        });

        // facet exit..
        graphSelect.exit().transition().duration(300).remove();

        var graphEnter = graphSelect.enter()
                .append("g").attr("class", function(d){return "graph " + d.key; })
                .attr("transform", function(d,i){
                    return translateStr(0, i*(sizeHeight+padding + titlePadding));
        });

        // each facet
        graphEnter.each(function(annotation){
            var thisGraph = d3.select(this);
            
            thisGraph.append("text")
                .attr("class","annotation title")
                .attr("x", 0)
                .attr("y", -10)
                .text(annotation.key);
            // data keyed by normalisation -> filename for this window
            var norms = find(annotation.values, "key", thisWindow).values;
            var N = getN(thisWindow, sizeHeight);
            bpObject.addBoxplotGroup(thisGraph, norms, "Filename", N);

            // add legend
            var legend = boxplot.svgg.selectAll("g.legend").data(showLegend, function(d){return d;});
            legend.transition().call(d3.legend);

            var legendEnter = legend.enter().append("g").attr("class", "legend")
                    .attr("transform", "translate(800,0)")
                    .call(d3.legend);  

            legend.exit().transition().remove();


            
        });
    };
    
    
    function getN(window)
    {
        var win = /([\d.]+) - ([\d.]+)/.exec(window);
        return win[1] - win[2];
    }

})();