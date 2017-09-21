// boxplots for fold change values over size classes
mboxplot = {};
(function () {
    mboxplot.margins = {"left": 100, "right": 40, "top": 40, "bottom": 40};
    mboxplot.dim = {"width": 800, "height": 250};
    mboxplot.titlePadding = 40;


    mboxplot.initialise = function ()
    {
        mboxplot.svg = d3.select("#mboxplot").append("svg").attr("class", "mboxplot_svg")
                //.attr("viewBox", "0 0 800 400")
                .attr("height", mboxplot.dim.height + mboxplot.margins.top + mboxplot.margins.bottom)
                .attr("width", mboxplot.dim.width + mboxplot.margins.left + mboxplot.margins.right);
        mboxplot.svgg = mboxplot.svg
                .append("g")
                .attr("transform", "translate(" + mboxplot.margins.left + "," + (mboxplot.margins.top + mboxplot.titlePadding) + ")");
    };
    
    mboxplot.build = function(json)
    {
//        var minPadding = 1;
//        var boxWidth = 10;
//        var endWidth = boxWidth;
        
        var chartvar = "Pair";
        var groupvar = "Normalisation";
        var boxvar = "Size";
        
//        setwidths = function (step)
//        {
//            if (step < boxWidth + minPadding) {
//                boxWidth = step - minPadding;
//                endWidth = boxWidth;
//            }
//        };
                    
        var charts = d3.nest().key(function(d){return d[chartvar];}).entries(json);
        var thischart = charts[0].values;

        thischart = options.filterNormArray(thischart);
        var getRange = function (n, i, useOutliers)
        {
            var get = (i === "Min" ? d3.min : d3.max);
            var b = n.Boxplot;
            if (!useOutliers)
            {
                return b[i];
            }
            else
            {
                return get([b[i], get(b.Outliers)]);
            }
        };

        var min = d3.min(thischart , function(x){ return getRange(x, "Min", true);});
        var max = d3.max(thischart , function(x){ return getRange(x, "Max", true);});

        var yscale = d3.scale.linear()
                .range([mboxplot.dim.height - mboxplot.titlePadding, 0]).domain([min, max]);



        var sizes = thischart.map(function(x){return x[groupvar];});

        var xscale = d3.scale.ordinal()
                .rangeBands([0, mboxplot.dim.width], 0.4).domain(sizes);

        var xAxis = d3.svg.axis()
                .scale(xscale)
                .orient("top");

        var yAxis = d3.svg.axis()
                .scale(yscale)
                .orient("left");

        // Draw axes
        // x axis
        var xaxisSelect = mboxplot.svgg.selectAll("g.x.axis").data([xAxis], function (x, i) {
            return i;
        });

        xaxisSelect.enter().append("g").attr("class", "x axis")

                .call(function (x) {return x;});

        xaxisSelect.transition().call(xAxis);
        xaxisSelect.exit().transition().style("opacity", 0).duration(300).remove();

        // y axis
        var yaxisSelect = mboxplot.svgg.selectAll("g.y.axis").data([yAxis], function (x, i) {
            return i;
        });
        yaxisSelect.enter().append("g").attr("class", "y axis")
                .call(function (x) {return x;});

        yaxisSelect.transition().call(yAxis);
        yaxisSelect.exit().transition().style("opacity", 0).duration(300).remove();

        console.log(xscale.range());

        mboxplot.svgg.selectAll(".midline").data([true], function(d){return true}).enter().append("line")
                 .attr("class", "midline")
                 .attr("y1", function(d){return yscale(d);})
                 .attr("y2", function(d){return yscale(d);})
                 .attr("x1", 0).attr("x2", mboxplot.dim.width);
        mboxplot.svgg.selectAll(".midline").attr("y1", function(d){ return yscale(0)}).attr("y2", function(d){ return yscale(0)});


        var thisnorm_nested = d3.nest().key(function(x) { return x[groupvar];}).entries(thischart);
        console.log(thisnorm_nested);
        // per group boxplot plotting
        var group = mboxplot.svgg.selectAll("g.group")
                .data(thisnorm_nested, function (n) {
                    return n.key;
                });

        group.exit().transition().style("opacity", 0).duration(300).remove();

        group.transition().each(function (nd) {
            var bw = d3.select(this);

            var x = d3.scale.ordinal().rangeBands([xscale(nd.key), xscale(nd.key) + xscale.rangeBand()], 0, 1);
            x.domain(nd.values.map(function (n) {return n[boxvar];}));

            bw.select(".xx").remove();
            bw.append("g").attr("class","xx axis").call(d3.svg.axis().scale(x).orient("bottom")).attr("transform", translateStr(0, mboxplot.dim.height));

            // shift groups on x axis
            var bwg = bw.selectAll("g.bw");
            bwg.transition().attr("transform",  function(d){return "translate("+x(d[boxvar])+",0)";}).duration(300);   

            // update x positions of everything within groups, and plotting on y axis
            var bwgt = bwg.transition().duration(300);

            bwgt.select("rect.box").attr("x", boxx(x)).attr("width", boxWidth(x)).attr("y", boxy).attr("height", boxHeight);

            // quartile markers
            bwgt.selectAll("line.Q").attr("x1", endx1(x)).attr("x2", endx2(x));
            bwgt.selectAll("line.min").attr("y1", endymin).attr("y2", endymin);
            bwgt.selectAll("line.med").attr("y1", endymed).attr("y2", endymed);
            bwgt.selectAll("line.max").attr("y1", endymax).attr("y2", endymax);

            // whiskers
            bwgt.selectAll("line.whisker").attr("y1", endymax).attr("y2", endymin);

            //circles
            bwgt.selectAll("g.outliers circle").attr("cy", function(d){return yscale(d);});                

        });



        var groupEnter = group.enter().append("g").attr("class", function (d) {
            return "group " + d.key;
        });
                // .attr("transform", function (d) {return "translate(" + xscale(d.key) + "0)";})

                // For each group, plot a group of boxplots
        groupEnter.each(function (nd) {
                    var bw = d3.select(this);

                    // scale for this group to control where boxplots are positioned
                    var x = d3.scale.ordinal().rangeBands([xscale(nd.key), xscale(nd.key) + xscale.rangeBand()], 0, 1);
                    x.domain(nd.values.map(function (n) {return n[boxvar];}));
//                        setwidths(x(x.domain()[1]) - x(x.domain()[0]));

                    bw.append("g").attr("class","xx axis").call(d3.svg.axis().scale(x).orient("bottom")).attr("transform", translateStr(0, mboxplot.dim.height));

                    // add a g element per boxplot datum
                    var bwEnter = bw.selectAll("g.bw").data(nd.values, function (f) {return f[boxvar];})
                            // this g represents the whole boxplot
                            .enter().append("g").attr("class", function (d) {return "bw " + d.Pair;})
                            // translate this boxplot to right place on the scale
                            .attr("transform", function (d) {
                                return translateStr(x(d[boxvar]) + x.rangeBand()/2, 0);
                            });
                    //bw.attr("transform", translateStr(x.rangeBand()/2,0));


                    // The whiskers
                    bwEnter.append("line").attr("class", "whisker")
                            // no x coordinates because of x translation of the parent g
                            .attr("y1", endymax)
                            .attr("y2", endymin);

                    // Min marker
                    bwEnter.append("line").attr("class", "min Q")
                            .attr("x1", endx1(x))
                            .attr("x2", endx2(x))
                            .attr("y1", endymin)
                            .attr("y2", endymin);

                    // Max marker
                    bwEnter.append("line").attr("class", "max Q")
                            .attr("x1", endx1(x))
                            .attr("x2", endx2(x))
                            .attr("y1", endymax)
                            .attr("y2", endymax);

                    // Box marker (covers middle section of whisker)
                    bwEnter.append("rect").attr("class", "box")
                            .attr("width", boxWidth(x))
                            .attr("x", boxx(x))
                            .attr("y", boxy)
                            .attr("height", boxHeight);
//                                .style("fill", function (d) {
//                                    return colours(d.Pair);
//                                }).attr("data-legend", function (d) {
//                            return d.Filename;
//                        });

                    // Median marker (drawn on top of box)
                    bwEnter.append("line").attr("class", "med Q")
                            .attr("x1", endx1(x))
                            .attr("x2", endx2(x))
                            .attr("y1", endymed)
                            .attr("y2", endymed);

                    bwEnter.append("g").attr("class", "outliers")
                            //       .attr("transform", function (d) {return "translate(" + x(d.Filename) + ",0)";})
                            .each(function (bwd)
                            {
                                var thisBw = d3.select(this);
                                thisBw.selectAll("circle").data(bwd.Boxplot.Outliers, function (d, i) {
                                    return i;
                                }).enter().append("circle")
                                        .attr("cx", 0)
                                        .attr("cy", function (d) {
                                            return yscale(d);
                                        })
                                        .attr("r", 1.5);
                            });
                });
        function boxWidth (d) {
            return d.rangeBand();
        };
        function boxx(x) {
            return 0 - x.rangeBand() / 2;
        }
        function boxy(d) {
            return yscale(d.Boxplot.UQ);
        }
        function boxHeight(d) {
            return yscale(d.Boxplot.LQ) - yscale(d.Boxplot.UQ);
        }
        function endx2(x) {
            return 0 + x.rangeBand() / 2;
        }
        function endx1(x) {
            return 0 - x.rangeBand() / 2;
        }
        function endy(d, i) {
            return yscale(d.Boxplot[i]);
        }
        function endymin(d) {
            return endy(d, "Min");
        }
        function endymax(d) {
            return endy(d, "Max");
        }
        function endymed(d) {
            return endy(d, "MED");
        }
    };

    
})();


