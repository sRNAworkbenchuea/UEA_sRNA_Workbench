// boxplots for fold change values over size classes
scd = {};
(function () {
    scd.margins = {"left": 150, "right": 40, "top": 40, "bottom": 40};
    scd.dim = {"width": 1800, "height": 400};
    scd.titlePadding = 40;
    
    var initialised = false;
    
    // maximum space for each size on graph
    var maxXspacing = 30;
    
    // TODO: Dynamically window with max height of nice looking
    // graph and facet into min height before requiring to scroll
    var sizeHeight=150;
    var padding = 40;
    var titlePadding = 10;
    var yAxisPadding = 15; // padding to leave room for y axis
    
    function groupBy(array, f)
    {
        var groups = {};
        array.forEach(function(o)
        {
            var group = JSON.stringify(f(o));
            groups[group] = groups[group] || [];
            groups[group].push(o);
        });
        return Object.keys(groups).map(function(group)
        {
            return groups[group];
        });
    }
    //the name of the stage that generated this data
    var stageName = "";
    
    //the funtion to set the stage name
    scd.setStageName = function(newStageName)
    {
        stageName = newStageName;
    };
    
    scd.initialise = function ()
    {
        scd.svg = d3.select("#scd_div").append("svg").attr("class", "scd_svg").attr("id", "scd")
                //.attr("viewBox", "0 0 800 400")
                .attr("height", scd.dim.height + scd.margins.top + scd.margins.bottom)
                .attr("width", scd.dim.width + scd.margins.left + scd.margins.right);
        scd.svgg = scd.svg
                .append("g")
                .attr("transform", "translate(" + scd.margins.left + "," + 
                    (scd.margins.top + scd.titlePadding) + ")");
        scd.svgg.append("g")
                .attr("class", "legend");
            
        initialised = true;
    };
    
    scd.isinitialised = function()
    {
        return initialised;
    };
    
    scd.build = function(json, asPercentage)
    {
            // variable to toggle all graphs by. This should be a toggle with only one selected at a time
            var toggleVar = "Normalisation"; 
            // variable to vertically split graphs by. Multiple splits can be selected to get multiple graphs
            var splitVar = "Annotation" ;
            // nest data with top level toggling variable and the
            // next level being the split variable
            var nested = d3.nest().key(function(d){return d[toggleVar]; })
                    .key(function(d){return d[splitVar];}).entries(json);
            
            // filter the array for the toggled variable
            // Because this SHOULD be a single value, take the first from the output array
            var toggledData = options.filterArray(nested, "key", options.normTypes)[0];
            
            // filter array for selected split-by variables
            var split_filtered = options.filterArray(toggledData.values, "key", options.annotationTypes);
            
            // resize svg height based on number of facets
            var newHeight = split_filtered.length*(sizeHeight+padding+titlePadding) + scd.margins.top + scd.margins.bottom;
            if(scd.svg.attr("height") < newHeight)
                scd.svg.attr("height", newHeight);
            
            var samples = findUnique(json, "Sample");
            
            var yVar = options.sizeClassDataType;
            
            var percent = [];
            var sampleAndSize = d3.nest().key(function(d){return d[toggleVar]}).key(function(d){return d.Filename}).key(function(d){return d.Size}).entries(json);
            
            sampleAndSize.forEach(function(norm){
                norm.values.forEach(function(filename){
                    filename.values.forEach(function(size){
                        var sample = filename.values[0].values[0].Sample;
                        var totals =  {Mapped:{Redundant:0, Nonredundant:0}, All:{Redundant:0, Nonredundant:0}};
                        size.values.forEach(function(d){

                            if(d.Annotation === "Mapped" || d.Annotation === "All")
                            {
                                totals[d.Annotation].Redundant += d.Redundant;
                                totals[d.Annotation].Nonredundant += d.Nonredundant;
                            }

                        })
                        percent.push({Filename: filename.key,Sample:sample,
                        Size: size.key,
                        Normalisation:norm.key,
                        Annotation:"Percentage Mapped",
                        Redundant: totals.Mapped.Redundant / totals.All.Redundant,
                        Nonredundant: totals.Mapped.Nonredundant / totals.All.Nonredundant})
                    })
                })
            })
            if(asPercentage === true){
                split_filtered = d3.nest().key(function(d){return d[toggleVar]}).key(function(d){return d[splitVar]}).entries(percent);
                split_filtered = options.filterArray(split_filtered, "key", options.normTypes)[0].values;
            }

            var xscale = d3.scale.linear().domain([d3.min(json, function(d){return d.Size;}),
                d3.max(json, function(d){return d.Size;})]);
            
            var numSizes = xscale.domain()[1] - xscale.domain()[0];
            var xWidth = numSizes*maxXspacing;
            xscale.range([yAxisPadding,xWidth]);
            
            var filecol = getWbColourScale(samples.length).domain(samples); 

            var xTicks = [];
            for(var i=xscale.domain()[0]; i <= xscale.domain()[1]; i++)
            {
                xTicks.push(i);
            }            
            
            // plot each split, where d is an array containing one data object for each graph
            var plotSplits = function(d){

            
                var splitSelect = d3.select(this).selectAll(".split")
                        .data(split_filtered, function(d){return d.key;});
                

            
                // facet transition..         
                splitSelect.transition().attr("transform", function(d,i){
                    return translateStr(0, i*(sizeHeight+padding + titlePadding));
                })
                .each(function(a) // update contents of each facet (y axis and lines)
                {
                    var yscale = d3.scale.linear().domain([
                        d3.min(a.values, function(d){return d[yVar];}),
                        d3.max(a.values, function(d){return d[yVar];})])
                        .range([sizeHeight, 0]);
                    d3.select(this).select(".y").transition().duration(300).call(d3.svg.axis().scale(yscale).ticks(4).orient("left"));
                    var files =  d3.nest().key(function(dd){return dd.Filename;}).entries(a.values);
                    d3.select(this).selectAll(".sl").data(files, function(f){return f.key;})
                            .transition().duration(300).attr("d", function(f){ return aline(yVar, xscale, yscale)(f.values);});
                });

                // facet exit..
                splitSelect.exit().transition().duration(300).remove();

                // facet enter...
                var splitEnter = splitSelect.enter()
                        .append("g").attr("class", function(d){ return "split " + d.key })
                        .attr("transform", function(d,i){
                            // move facet in to position
                            return translateStr(0, i*(sizeHeight+padding + titlePadding));
                        });  

                // each plot
                splitEnter.each(function(d){
                    var yscale = d3.scale.linear().domain([
                        d3.min(d.values, function(d){return d[yVar];}),
                        d3.max(d.values, function(d){return d[yVar];})])
                        .range([sizeHeight, 0]);
                    var thisSplit = d3.select(this);

                    // draw x scale
                    thisSplit.append("g").attr("class", "x axis")
                        .attr("transform", translateStr(0, sizeHeight))
                        .call(d3.svg.axis().scale(xscale).tickValues(xTicks).tickFormat(d3.format("d")).orient("bottom"));
                    // draw y scale
                    thisSplit.append("g").attr("class", "y axis")
                        .attr("transform", translateStr(0, 0))
                        .call(d3.svg.axis().scale(yscale).ticks(4).orient("left")); 

                    // annotation title
                    thisSplit.append("text").attr("class", "title")
                            .attr("transform", translateStr(xWidth/2, 0))
                            .text(d.key).attr("text-anchor","middle");

                    // x grid lines
                    thisSplit.selectAll(".gline").data(xTicks)
                            .enter().append("line").attr("class", "gline")
                            .attr("x1", function(d){return xscale(d)})
                            .attr("x2", function(d){return xscale(d)})
                            .attr("y1", function(d){return yscale.range()[0]})
                            .attr("y2", function(d){return yscale.range()[1]})
                            .style("stroke", "grey")
                            .style("stroke-width", "1px")
                            .style("stroke-dasharray", "5,10");

                    var files =  d3.nest().key(function(dd){return dd.Filename;}).entries(d.values);
                    var lines = thisSplit.selectAll(".sl")
                            .data(files, function(d){return d.key}).enter();
                    
                    var paths = lines.append("path")
                            .attr("class", "sl")
                            .attr("data-legend", function(d){return d.values[0].Sample})
                            .attr("data-legend-pos", function(d,i){return i})
                            .attr("d", function(d){return aline(yVar, xscale, yscale)(d.values);})
                            .attr("stroke", function(d){return filecol(d.values[0].Sample);})
                            .attr("fill", "none")
                            .style("stroke-width", "1.5px");
                    
                    paths.transition().duration(300).attr("d", function(d){return aline(yVar, xscale, yscale)(d.values)});

                });
        };
        var toggleSelect = scd.svgg.selectAll(".toggle").data([split_filtered], function(d){return toggledData.key;});
        
        toggleSelect.enter().append("g").attr("class", "toggle").each(plotSplits);
        toggleSelect.transition().each(plotSplits);
        
        d3.select(".legend").remove();
        var legend = scd.svgg.append("g").attr("class", "legend")
                //.attr("transform", translateStr(scd.margins.left + yAxisPadding + xWidth, scd.svg.attr("height")/2))
                .style("font-size", "12px");
        legend.call(d3.legend);
        legend.attr("transform", translateStr(scd.margins.left + yAxisPadding + xWidth, 0));
            
        // line drawing function
        function aline(yVar, xscale, yscale){
            return d3.svg.line()
                .x(function (d) {return xscale(d.Size);})
                .y(function (d) {return yscale(d[yVar]);});
        }
//        });
    };
})();
