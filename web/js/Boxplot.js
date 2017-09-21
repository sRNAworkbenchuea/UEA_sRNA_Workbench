function Boxplot(id, options, showLabels, showLegend, showN, drawXaxis){

    // the id for this Boxplot. i.e. the window for Abundance Boxplot.
    var id = id;
    
    var maxGraphWidth = options.maxGraphWidth;
    var maxBoxWidth = options.maxBoxWidth;
    var graphHeight = options.graphHeight;
    
    var drawXaxis = drawXaxis// draw the boxplot x axis
    
    // spacing between boxes
    var spacing = options.spacing;
    var showLabels = showLabels;
    var showLegend=showLegend;
    var showN = showN;
    var transitionDuration = options.transitionDuration;
    
    this.addBoxplotGroup = function(selection, data, xKey, N, lines, yintercept)
    {
        var graphWidth = calculateGraphWidth(data, maxGraphWidth, maxBoxWidth)
        // x scale
        var xscale = getXscale(data, graphWidth);

        // find y scale
        var yscale = getYscale(data, graphHeight);

        // N scale if needed
        var nscale = getNscale(N, graphHeight);
        
        var uxBits = {};
        data[0].values.forEach(function(d){
            uxBits[d[xKey]] = 1;
        })
        var colours = getWbColourScale(Object.keys(uxBits).length).domain(Object.keys(uxBits));

        // per group boxplot plotting
        var group = selection.selectAll("g.group").data(data, function (n) {return n.key;});
        
        
        // Boxplot transitions
        // updates to shift groups along the x axis to make way for incoming and outgoing groups
        // updates to replot everything on y axis when y scale changes
        group.transition().each(function (normData) {
            var thisNormSelection = d3.select(this);
            // setup and position a new x scale for this group.
            var x = d3.scale.ordinal().rangeBands([xscale(normData.key), xscale(normData.key) + xscale.rangeBand()], spacing);
            x.domain(normData.values.map(function (n) {
                return n[xKey];
            }));
            


            // n scale for when the N option is turned on
            var n = d3.scale.ordinal().rangeBands([xscale(normData.key), xscale(normData.key) + xscale.rangeBand()], 0);
            n.domain(normData.values.map(function (n) {
                return n[xKey];
            }));

            // update sample labels
            addBpLabels(thisNormSelection, showLabels, x, yscale);
            addNBar(thisNormSelection, n, showN, transitionDuration);
            // shift groups on x axis
            var boxplotSelection = thisNormSelection.selectAll("g.bw").data(normData.values, function(d){return d[xKey]});
            boxplotSelection.exit().transition().style("opacity", 0).duration(transitionDuration).remove();
            addBoxplots(thisNormSelection, normData, xKey, x, yscale, n, colours)

            // draw the lower x axis
            thisNormSelection.select(".xx").remove();
            if(drawXaxis){ 
                thisNormSelection.append("g").attr("class","xx axis").call(d3.svg.axis().scale(x).orient("bottom")).attr("transform", translateStr(0, graphHeight));
            }
            
            var boxplotTransition = boxplotSelection.transition();

            boxplotTransition.attr("transform", function (d) {
                return "translate(" + x(d[xKey]) + ",0)";
            }).duration(transitionDuration);

            // update x positions of everything within groups, and plotting on y axis
            //var bwgt = bwg.transition().duration(transitionDuration);
            boxplotTransition.select("rect.box")
                    .attr("x", boxx(x))
                    .attr("width", boxWidth(x))
                    .attr("y", function(d){return boxy(d,yscale) })
                    .attr("height", function(x){return boxHeight(x,yscale);})
                    .style("fill", function (d) {
                        if (showLegend[0]) {return colours(d[xKey]);}
                        return getThemeColour();
                    });

            var ymin = function(d){ return endy(d, "Min", yscale);};
            var ymax = function(d){ return endy(d, "Max", yscale);};
            var ymed = function(d){ return endy(d, "MED", yscale);};

            // quartile markers
            //boxplotTransition.selectAll("line.Q").attr("x1", endx1(x)).attr("x2", endx2(x));
            boxplotTransition.select("line.min")
                    .attr("x1", endx1(x)).attr("x2", endx2(x))
                    .attr("y1", ymin).attr("y2", ymin);
            boxplotTransition.select("line.med")
                    .attr("x1", endx1(x)).attr("x2", endx2(x))
                    .attr("y1", ymed).attr("y2", ymed);
            boxplotTransition.select("line.max")
                    .attr("x1", endx1(x)).attr("x2", endx2(x))
                    .attr("y1", ymax).attr("y2", ymax);

            // whiskers
            boxplotTransition.select("line.whisker")
                    .attr("x1", whiskerx(x))
                    .attr("x2", whiskerx(x))
                    .attr("y1", ymax).attr("y2", ymin);

            //circles
            boxplotSelection.selectAll("g.outliers").data(normData.values, function (d, i){return d[xKey]})
                    .each(function (d) {
                // remove any circles that are no longer there due to change in window
                var bwout = d3.select(this).selectAll("circle").data(d.Boxplot.Outliers, function (d, i) {
                    // return a key of index + window to ensure circles are removed and replaced correctly with
                    // differing windows
                    return id + i;
                });
                bwout.exit().transition().style("opacity", 0).duration(transitionDuration).remove();
                bwout.transition().attr("cy", function (d) {
                    return yscale(d);
                }).attr("cx", whiskerx(x)).duration(transitionDuration);
                bwout.enter().append("circle")
                        .attr("cx", whiskerx(x))
                        .attr("cy", function (d) {
                            return yscale(d);
                        })
                        .attr("r", 0.5);
            });

        });

        group.exit().transition().style("opacity", 0).duration(transitionDuration).remove();

        // Enter selection
        var groupEnter = group.enter().append("g").attr("class", function(d) {return "group "+d.key;})
            // .attr("transform", function (d) {return "translate(" + xscale(d.Normalisation) + "0)";})

            // For each group, plot a group of boxplots
            .each(function (nd) {
                 var bw = d3.select(this);

                 // scale for this group to control where boxplots are positioned on the grouping scale
                 var x = d3.scale.ordinal().rangeBands([xscale(nd.key), xscale(nd.key) + xscale.rangeBand()],spacing);
                 x.domain(nd.values.map(function(n){ return n[xKey];}));

                 var n = d3.scale.ordinal().rangeBands([xscale(nd.key), xscale(nd.key) + xscale.rangeBand()], 0);
                 n.domain(nd.values.map(function (n) { return n[xKey]; }));

                 addBpLabels(bw, showLabels, x,yscale);
                 addNBar(bw, n, showN, transitionDuration);
                 
                 if(yintercept !== undefined){
                    var y_intercept_line = bw.selectAll(".yintercept").data(yintercept).enter().append("line")
                            .attr("class", "yintercept")
                            .attr("x1", 0).attr("x2", graphWidth)
                            .attr("y1", function(d){return yscale(d)}).attr("y2", function(d){return yscale(d)})
                               .attr("stroke", "#686868")
                               .attr("fill", "none")
                               .style("stroke-width", "1px");
                }
                 
                 var theseLines = undefined;
                 if(lines !== undefined){
                    theseLines = find(lines.values, "key", nd.key)
                 }
                 addBoxplots(bw, nd, xKey, x, yscale, n, 0, theseLines, colours)
                 
                // draw the lower x axis
                bw.select(".xx").remove();
                if(drawXaxis){ 
                    bw.append("g").attr("class","xx axis").call(d3.svg.axis().scale(x).orient("bottom")).attr("transform", translateStr(0, graphHeight));
                }

             }).attr("opacity", 0).transition().attr("opacity",1).duration(transitionDuration);

        //x axis
        var xGroupAxis = d3.svg.axis().scale(xscale).orient("top");

        var xaxisSelect = selection.selectAll("g.xgroup.axis")
                .data([xGroupAxis], function (x, i) {return i;});

        xaxisSelect.enter().append("g").attr("class", "xgroup axis")
                .attr("transform", "translate(0,0)")
                .call(function (x) {return x;});

        xaxisSelect.transition().call(xGroupAxis);
        xaxisSelect.exit().transition().style("opacity", 0).duration(transitionDuration).remove();



        // y axis
        var yAxis = d3.svg.axis().scale(yscale).orient("left");

        var yaxisSelect = selection.selectAll("g.y.axis")
                .data([yAxis], function (x, i) {return i;});

        yaxisSelect.enter().append("g").attr("class", "y axis")
                .call(function (x) {return x;});

        yaxisSelect.transition().call(yAxis);
        yaxisSelect.exit().transition().style("opacity", 0).duration(transitionDuration).remove(); 
    };
    
    function addBoxplots(selection, data, xKey, x, yscale, nscale, n, lines, colours){
        var aline = d3.svg.line()
                    .x(function(d){return x(d.Sample) + x.rangeBand()/2;})
                    .y(function(d){return yscale(d.Exp);});
        if(lines !== undefined){
            selection.selectAll(".seqline").data(lines.values, function(d){return d.Seq})
                    .enter().append("path").attr("class", "seqline")
                    .attr("d", function(d){return aline(d.Values)})
                    .attr("stroke", "#686868")
                    .attr("fill", "none")
                    .style("stroke-width", "1px")  
        };
        // add a g element per boxplot datum
        var bwEnter = selection.selectAll("g.bw").data(data.values, function(d){return d[xKey];})
                // this g represents the whole boxplot
                .enter().append("g").attr("class", function(d){return "bw "+d[xKey]})
                // translate this boxplot to right place on the scale
                .attr("transform",  function(d){return "translate("+x(d[xKey])+",0)";});

        if(options.bpShowNBar === true){
            // N bar
            bwEnter.append("rect")
                    .attr("class", "N")
                    .attr("x", boxx(n))
                    .attr("width", boxWidth(n))
                    .attr("y", function(d){ return ny(d,nscale)})
                    .attr("height", function(d){return nHeight(d,nscale)});
        }


        // The whiskers
        bwEnter.append("line").attr("class", "whisker")
                // no x coordinates because of x translation of the parent g
                .attr("x1", whiskerx(x))
                .attr("x2", whiskerx(x))
                .attr("y1", function(d){return endy(d, "Max", yscale)})
                .attr("y2", function(d){return endy(d, "Min", yscale)});

        // Min marker
        bwEnter.append("line").attr("class", "min Q")
                .attr("x1", 0)
                .attr("x2", x.rangeBand())
                .attr("y1", function(d){return endy(d, "Min", yscale)})
                .attr("y2", function(d){return endy(d, "Min", yscale)});

        // Max marker
        bwEnter.append("line").attr("class", "max Q")
                .attr("x1", 0)
                .attr("x2", x.rangeBand())
                .attr("y1", function(d){return endy(d, "Max", yscale)})
                .attr("y2", function(d){return endy(d, "Max", yscale)});

        // Box marker (covers middle section of whisker)
        bwEnter.append("rect").attr("class", "box")
                .attr("x", boxx(x))
                .attr("width", boxWidth(x))
                .attr("y", function(d){return boxy(d,yscale);})
                .attr("height", function(d) {return boxHeight(d, yscale);})
                .style("fill", function(d){
                    if(showLegend[0]){
                        return colours(d[xKey]); 
                    }; 
                    return getThemeColour();
                 })
                .attr("data-legend", function(d){
                    return d[xKey];
        })
              .attr("data-legend-pos", function(d,i){return i;});


        // Median marker (drawn on top of box)
        bwEnter.append("line").attr("class", "med Q")
                .attr("x1", 0)
                .attr("x2", x.rangeBand())
                .attr("y1", function(d){return endy(d, "MED", yscale)})
                .attr("y2", function(d){return endy(d, "MED", yscale)});

        bwEnter.append("g").attr("class", "outliers")
        .each(function (bwd)
        {
            var thisBw = d3.select(this);
            thisBw.selectAll("circle").data(bwd.Boxplot.Outliers, function (d, i) {
                // return a key of index + window to ensure circles are removed and replaced correctly with
                // differing windows
                return id+i;
            }).enter().append("circle")
                    .attr("cx", whiskerx(x))
                    .attr("cy", function (d) {
                        return yscale(d);
                    })
                    .attr("r", 0.5).style("stroke", "black");
        });
    }    
    // helper functions to reduce attribute calls for enter and update
    // to one-lines
    function boxx(d) { return 0; };
    function boxWidth(d) { return d.rangeBand() ; };
    function boxy(d, y) {
        var b = d.Boxplot;
        return y(b.UQ);
    };
    function boxHeight(d, y) {
        var b = d.Boxplot;
        return y(b.LQ) - y(b.UQ);
    };
    function nHeight(d, nscale) {
        return nscale.range()[0] - nscale(d.Boxplot.N);
    };
    function ny(d, nscale) {
        return nscale(d.Boxplot.N);
    };
    function endx2(d) { return d.rangeBand(); };
    function endx1(d) { return 0; };
    function endy (d, i, y) { return y(d.Boxplot[i]); };
    //function endymin (d, y) { return endy(d, "Min", y);};
    //function endymax (d, y) { return endy(d, "Max", y); };
    //function endymed (d, y) { return endy(d, "MED", y); };
    function whiskerx(d) { return d.rangeBand()/2; };
   function addNBar(bw, n, showN, duration)
    {
        // N bar
        var bwSelect = bw.selectAll(".N").data(showN, function(x){return x});
        bwSelect.transition().duration(duration).attr("height", nHeight);
        var bwEnter= bwSelect.enter().append("rect")
                .attr("class", "N")
                .attr("x", boxx(n))
                .attr("width", boxWidth(n))
                .attr("y", ny)
                .attr("height", nHeight);
        bwSelect.exit().transition().attr("opacity", 0).duration(duration).remove();
    }  
    function addBpLabels(bw, showLabels, x, yscale)
    {
        var xBoxAxis = d3.svg.axis().scale(x).orient("bottom");  

        var xbaxisSelect = bw.selectAll("g.xbox").data(showLabels, function(x){return x});

        var xbaxisEnter = xbaxisSelect.enter().append("g").attr("class", "xbox axis")
                .attr("transform", translateStr(0, yscale.range()[0] + 20))
                .call(xBoxAxis);

        xbaxisEnter.selectAll("text")
                .attr("text-anchor", "end")
                .attr("dx", "-1.1em")
                .attr("dy", "-0.4em")
                .attr("transform", "rotate(-90)");

        xbaxisEnter.selectAll("path, line").style("display", "none");

        xbaxisSelect.exit().transition().attr("opacity",0).duration(400).remove();
    }
    
    function getBPrange (n, i, useOutliers)
    {
        var get = (i === "Min" ? d3.min : d3.max);
        return get(n.values, function(n){ 
                if (!useOutliers)
                {
                    return n.Boxplot[i];
                }
                else
                {
                    return get([n.Boxplot[i], get(n.Boxplot.Outliers)]);
                }
        });
    };  
    
    function getYscale(norms, sizeHeight) {
        // find y scale range
        var min = d3.min(norms, function (d) {
            return getBPrange(d, "Min", true);
        });
        var max = d3.max(norms, function (d) {
            return getBPrange(d, "Max", true);
        });
        // y scale
        return d3.scale.linear()
                .range([sizeHeight, 0]).domain([min, max]);
    }
    

    
    function getXscale(norms, width){
        return d3.scale.ordinal()
            .rangeBands([0, width], 0.05, 0.1)
            .domain(norms.map(function (n) {return n.key;}));
    }
    
    function getNscale(N, height) {
        return d3.scale.linear().range([height, 0])
                .domain([0, N]);
    }  
    
    function calculateGraphWidth(data, currentWidth, maxWidth) {
        // Calculate appropriate boxplot widths
        var numberOfBoxplots = 0;
        data.forEach(function (d) {
            d.values.forEach(function (dd) {
                numberOfBoxplots++;
            })
        })

        var boxplotSize = currentWidth / (numberOfBoxplots);

        var newWidth = currentWidth;
        if (boxplotSize * 0.75 > maxBoxWidth)
        {
            newWidth = (numberOfBoxplots * maxBoxWidth) + (numberOfBoxplots * maxBoxWidth * 0.25)
        }
        return newWidth;
    }

}

