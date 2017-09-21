
// containing object to prevent var conflicts
var maplot = {};
(function(){
maplot.normKeys = {
    "RAW":"NONE", 
    "PT":"PER_TOTAL", 
    "UQ":"UPPER_QUARTILE", 
    "TM":"TRIMMED_MEAN", 
    "Q":"QUANTILE", 
    "B":"BOOTSTRAP",
    "DE":"DESEQ"};
maplot.normToShow = Object.keys(maplot.normKeys);
maplot.groupBy = ["Normalisation", "Plot"];

//maplot.build = function()
//{
//    maplot.updateOptions();
//    maplot.update();
//};
// interfaces with local storage to update options
// that will change the plot
maplot.updateOptions = function()
{
    maplot.normToShow = [];
    Object.keys(maplot.normKeys).forEach(function(k)
    {
        var lsv = localStorage[k];
        if (lsv === "true")
        {
            maplot.normToShow.push(k);
        }
    });

    var gb = localStorage.groupBy;
    if(gb === "Plot")
        maplot.groupBy = ["Pair", "Normalisation"];
    else
        maplot.groupBy = ["Normalisation", "Pair"];
    
    // If this is undefined, the plots should be produced with the first offset
    maplot.offset = localStorage.maOffset;
    maplot.dataType = localStorage.maDataType;

};

maplot.axis = d3.svg.axis().ticks(4);

maplot.orderByVals = ["Normalisation", "Pair"];

maplot.initialised = false;


maplot.margins = {"left": 70, "right": 40, "top": 40, "bottom": 40};

maplot.initialise = function(){
    
    if(!maplot.initialised)
    {
        maplot.svg = d3.select("#maplot_div").append("svg:svg").attr("class", "maplot_svg").attr("id", "maplot")
                .attr("width", window.innerWidth);

        maplot.svgg = maplot.svg
                .append("svg:g")
                .attr("transform", translateStr(maplot.margins.left, maplot.margins.top));//.call(zoom.event);

        maplot.zoom = d3.behavior.zoom()
                .translate([maplot.margins.left,maplot.margins.top])
                .scale(1)
                .on("zoom", maplot.zoomed);
        maplot.initialised = true;
    }
};

// variables for the the individual plot size, padding, spacing between groups etc
var size = 200, padding = 15, titlepadding = 20, spacing = 50, normSpacing = 40, normTitlePadding = 20;
var maxSize = 300;
var nrows=0, ncols=0;

function getNewGroupSize(newSize)
{
    var factor = newSize/size;
    var newPadding = factor*padding, newSpacing = factor*spacing;
    return [ncols * (newSize + newPadding + newSpacing), nrows * (newSize + newPadding + newSpacing)];
}

// get size of a plot group based on the number of columns and rows
function getGroupDim(ncols, nrows){
    return [ncols * (size + padding + spacing), nrows * (size + padding + spacing)];
}
    // Resize height of svg to allow all plots to fit down the page
function resizeHeight(rowdim){
    d3.select(".maplot_svg").attr("height", nrowblocks * (rowdim + normTitlePadding + normSpacing) + maplot.margins.bottom);
        //    || normTitlePadding + normSpacing + maplot.margins.bottom);
}

// Calculates positions and draws all plots
maplot.build = function(json)
{   
    // Used for selecting plots/user interaction with functions below. Currently NOT working
    var active = d3.select(null);

    // Change key strings depending on whether we are plotting MAs or correlations
    var dx="A", dy="M";
    var dt="MA";
    if(maplot.dataType === "corr")
    {
        dx = "refExpression";
        dy = "obsExpression";
        dt = "Expression";
    }

    var offsets = d3.nest().key(function(d){return d.Offset;}).entries(json);
    // take the data corresponding to the selected offset
    // if offset was not selected, take the first set of data.
    var top = [];
    if(maplot.offset === undefined)
    {
        top = offsets[0].values;
    }
    else
    {
        top = find(offsets, "key", maplot.offset).values;
    }

    // Extract just the selected normalisations
    var normfiltered = [];

    options.normTypes.forEach(function(k)
    {
        top.forEach(function(n)
        {
            n.Annotation.forEach(function(a){

            })
            if (n.Normalisation === k)
            {
                normfiltered.push(n);
            }
        });
    });


    // filter all unticked annotation types
    var filtered = [];
    var uniqueAnnotations = {};
    if(options.annotationTypes.length > 0){
        normfiltered.forEach(function(n)
        {
            annotationFilter: for(var k = options.annotationTypes.length-1; k >= 0; k--)
            {
                var thisType = options.annotationTypes[k];
                for(var a=n.Annotation.length-1; a >= 0; a--)
                {
                    if(n.Annotation[a] === thisType)
                    {
                        uniqueAnnotations[thisType] = 1;
                        n.Annotation = thisType;
                        filtered.push(n);
                        break annotationFilter;
                    }
                }
            }
        });
    }
    else
    {
        filtered = normfiltered;
    }
            console.log(uniqueAnnotations);


    // merge array into unique normalisation, filetype elements
    var map = {};

    filtered.forEach(function(i)
    {

        var annotationObj = {Annotation: i.Annotation, Size:i.Size, AnnotationType:i.AnnotationType, MA: i[dt]};
        var mapkey = i.Normalisation+i.Pair;
        if(!map[mapkey])
        {
            map[mapkey] = {Pair: i.Pair, Normalisation: i.Normalisation, Annotations: [annotationObj]}; 
        }
        else
        {
            map[mapkey].Annotations.push(annotationObj);
        }
    });

    var filtered = [];
    d3.values(map).forEach(function(i)
    {
       filtered.push(i); 
    });

    // set up colour scale for annotation
    var annotationsUsed = [];
    options.annotationTypes.forEach(function(a){
        if(uniqueAnnotations[a] !== undefined)
        annotationsUsed.push(a)
    });

    // retrieve the workbench-style colour scale
    var wbcols = getWbColourRange(annotationsUsed.length-1);

    // push an initial black for the first annotation (usually All)
    wbcols.unshift("#000000");
    var colours =  d3.scale.ordinal().range(wbcols);

    colours.domain(annotationsUsed);
    console.log(colours.range())
    console.log(colours.domain())


    var domainA = [d3.min(filtered, function(p) { return d3.min(p.Annotations, function(a){ 
                return d3.min(a.MA, function(ma) {return ma[dx];}) 
            }); }),
                   d3.max(filtered, function(p) { return d3.max(p.Annotations, function(a){ 
                return d3.max(a.MA, function(ma) {return ma[dx];}) 
            }); })];

    var numpoints = [];
    var uniqueCheck = {};
    filtered.forEach(function(f){
        f.Annotations.forEach(function(a){
            var mx = d3.max(a.MA, function(ma){return ma[dx]});
            if(!(f.Pair in numpoints)){
                numpoints[f.Pair] = a.MA.length;
            }
            else{
                numpoints[f.Pair] += a.MA.length;
            }

//            a.MA.forEach(function (point){
//                var uniquekey = f.Pair+f.Normalisation+a.AnnotationType+point.M + "," + point.A;
//                if (uniquekey in uniqueCheck)
//                {
//                    uniqueCheck[uniquekey]++;
//                }
//                else{
//                    uniqueCheck[uniquekey] = 1;
//                }
//                if(uniqueCheck[uniquekey]>1)
//                {
//                    console.log([uniquekey, uniqueCheck[uniquekey]]);
//                }
//            })

//                if(mx > 15){
//                console.log([f.Pair,a.Annotation,a.AnnotationType, mx])
//                }
        })
    })
    //console.log(numpoints);
    //console.log(uniqueCheck);

    var domainM = [d3.min(filtered, function(p) { return d3.min(p.Annotations, function(a){ 
                return d3.min(a.MA, function(ma) {return ma[dy];}) 
            }); }),
                   d3.max(filtered, function(p) { return d3.max(p.Annotations, function(a){ 
                return d3.max(a.MA, function(ma) {return ma[dy];}) 
            }); })];

    var rangex = [padding / 2, size - padding / 2];
    var rangey = [padding / 2, (size - padding / 2) - titlepadding];
    var xAxis = d3.scale.linear().domain(domainA).range(rangex);
    var yAxis = d3.scale.linear().domain(domainM).range([rangey[1], rangey[0]]);        

    // point plotting function
    var plot_points = function(a, omap, selectThis)
    {
        var getOpacity = function(p){
            var k = p.M+","+p.A;
            return (omap[k].sum*0.3) / omap[k].count;
        };

        // DEBUG: print out max A levels
//            console.log([a,d3.max(a.MA, function(ma){return ma[dx]})])

        var points = d3.select(selectThis)
                .selectAll("circle");
        var newPoints = points.data(a.MA);
        var circles = newPoints.enter().append("circle");
                circles.attr("cx", function (d) { 
//                        if(d[dx] > 15){
//                            console.log(d);
//                        }
                    return xAxis(d[dx]);
                })
                .attr("cy", function (d) { return yAxis(d[dy]);})
                .attr("r", 1)
                .attr("fill", colours(a.Annotation))
//                    .attr("fill-opacity", getOpacity)
//                    .attr("stroke-opacity", getOpacity);
        if(a.Annotation !== "All")
            circles.attr("data-legend", a.Annotation);
    }; 

    var plot_annotations = function(plotSelection, annotationData, add){
        var opacityMap = {};
        annotationData.forEach(function(a){
            a.MA.forEach(function(ma){
                var makey = ma.M+","+ma.A;
                if(opacityMap[makey] === undefined)
                {
                    opacityMap[makey] = {sum : ma.Count, count : 1};
                }
                else
                {
                    opacityMap[makey].sum += ma.Count;
                    opacityMap[makey].count++;
                }
            })
        })

        plotSelection.selectAll("g").remove();
//            if(add)

        // DEBUG: find maximum A levels
//            annotationData.forEach(function(a){
//                console.log([a.AnnotationType, d3.max(a.MA, function(ma){return ma[dx]})])
//            })

        plotSelection.selectAll("g").data(annotationData, function(d){return d.AnnotationType+d.Size;})
                .enter().append("g")
                .attr("class", function(a){ return "annotation "+a.AnnotationType;})
                .each(function(d){plot_points(d, opacityMap, this)});
        //annot.exit().remove();
    }

    /*--------------------------*
     * Plot position processing
     *--------------------------*/
    // calculate appropriate number of rows and columns for grouped blocks
    var uniqueGroupings = {};
    var uniquePlots = {};
    var g = maplot.groupBy[0];
    var p = maplot.groupBy[1];
    filtered.forEach(function(i){
        uniqueGroupings[i[maplot.groupBy[0]]]=1;
        uniquePlots[i[maplot.groupBy[1]]]=1;
    });
    filtered.sort(function(a,b)
    {
        var gc = comp(a[g], b[g]);
        if(gc===0)
            return comp(a[p], b[p]);
        return(gc);
    });

    nrows = Math.floor(Math.sqrt(Object.keys(uniquePlots).length));
    ncols = Math.ceil(Object.keys(uniquePlots).length / nrows);
    var groupdim = getGroupDim(ncols, nrows);

    var ncolblocks = Math.floor(maplot.svg.attr("width") / ( groupdim[0] + normSpacing + maplot.margins.left + maplot.margins.right));       
    nrowblocks = Math.ceil(Object.keys(uniqueGroupings).length / ncolblocks);

    var prow=nrows-1, pcol=0, grow=0, gcol=0;
    var lastg, lastp;

    // for each plot, assign a group id and a plot id
    filtered.forEach(function(plot)
    {

        if (lastg === undefined)
        {
            plot.gid = [gcol, grow];
            uniqueGroupings[plot[g]] = [gcol,grow];
            plot.pid = [pcol, prow];
        }
        else
        {
            if (plot[g] === lastg)
            {
                if (prow === 0)
                {
                    prow = nrows - 1;
                    pcol++;
                }
                else
                {
                    prow--;
                }
                plot.gid = [gcol, grow];
                plot.pid = [pcol, prow];

            }
            else
            {
                prow = nrows - 1, pcol = 0;

                if (gcol >= (ncolblocks - 1))
                {
                    gcol = 0;
                    grow++;
                }
                else
                {
                    gcol++;
                }
                plot.gid = [gcol, grow];
                uniqueGroupings[plot[g]] = [gcol,grow];

                plot.pid = [pcol, prow];
            }
        }
        lastg = plot[g];
        lastp=plot[p];
    });

    // List of axes positions
    var xAxisPos = [];
    var yAxisPos = [];

    var x =0, y =0;
    while(x<ncols)
    {
        xAxisPos.push([x++,0]);

    }
    while(y<nrows)
    {
        yAxisPos.push([0,y++]);
    }

    // The height of the SVG is resized to fit all plots
    resizeHeight(groupdim[1]);

    /*-----------------------------*
     * D3 plotting
     *-----------------------------*/
    var plots = maplot.svgg.selectAll(".plot")
            .data(filtered, function(d){ return d[g]+d[p];});

    // Top level translation update of plots
    plots.transition().attr("transform", function (d)
    {
        return translateStr((d.gid[0] * (groupdim[0]+normSpacing)) + (d.pid[0] * (size + spacing)), 
                            (d.gid[1] * (groupdim[1]+normSpacing+normTitlePadding)) + (d.pid[1] * (size + spacing)));
    }).duration(600);

    // Point and plot element updates
    plots.each(function(d){
        var thisPlot = d3.select(this);

        // DEBUG: prints out max A levels for each anntoation
//            d.Annotations.forEach(function(a){
//                console.log([a.Annotation, a.AnnotationType, d3.max(a.MA, function(ma){return ma[dx]})])
//            })

        // Plot title
        // FIXME: the plot title text ends up being ALL removed and then re-added
        // I want to just update the text that has changed (via a change in group by)
        thisPlot.append("text").attr("class", "ma title plot").attr("x", size / 2).attr("y", titlepadding / 1.5).attr("text-anchor", "middle")
                .style("font-size", "10px")
                .text(function(d) { return d[p]; });

        plot_annotations(thisPlot.select("g.plotarea"), d.Annotations)
        // Annotation selection

        // Update position of dotted lines
        var xr = xAxis.domain();
        var yr = yAxis.domain();         
        d3.select(this).select("g").selectAll(".mline").transition()
                .attr("x1", xAxis(xr[0]))
                .attr("x2", xAxis(xr[1]))
                .attr("y1", maplot.dataType === "corr" ? yAxis(yr[0]) : yAxis(0))
                .attr("y2", maplot.dataType === "corr" ? yAxis(yr[1]): yAxis(0))
                .duration(400);   
    });

    //** Top level enter for incoming plots ***//
    var plotEnter = plots.enter().append("svg:g")
            .attr("class", "plot")
            .attr("transform", function(d)
            {
                return translateStr((d.gid[0] * (groupdim[0]+normSpacing)) + (d.pid[0] * (size + spacing)), 
                                    (d.gid[1] * (groupdim[1]+normSpacing+normTitlePadding)) + (d.pid[1] * (size + spacing)));
            })
            .style("opacity",0);
    plotEnter.each(plot).transition().style("opacity",1).duration(200);
    //plotEnter.append("rect").attr("class", "clickable").attr("width", groupdim[0]).attr("height", groupdim[1]).on("click", clicked);

    //exit - fade removed plots out and then remove them
    plots.exit().transition().style("opacity", 0).duration(200).remove();

//        var pTitlex = function(d){return (d.gid[0] * (groupdim[0]+normSpacing)) + d.pid[0]*(size) + size/2;};
//        var pTitley = function(d){return (d.gid[1] * (groupdim[1]+normSpacing+normTitlePadding)) + d.pid[1]*(size) + titlepadding/1.5;};

//        var plotText = maplot.svg.selectAll(".ma.title.plot").data(filtered, function(d){ return d[g]+d[p];});
//        plotText.attr("x", pTitlex).attr("y", pTitley).text(function(d){return d[p];});
//        plotText.enter().append("text").attr("class","ma title plot").attr("x", pTitlex).attr("y", pTitley).attr("text-anchor", "middle")
//                .text(function(d){return d[p];});
//        plotText.exit().remove();

    var groups = maplot.svgg.selectAll("g.bits").data(Object.keys(uniqueGroupings).sort(), function(d){ return d;});

    var gTitlex = function(d){return (0.5*groupdim[0]);};
    var gTitley = function(d){return (-5);};

    groups.transition()
        .attr("transform", function(d)
        {
            return translateStr((uniqueGroupings[d][0])*(groupdim[0] + normSpacing), uniqueGroupings[d][1]*(groupdim[1]+normSpacing+normTitlePadding));
        }).duration(600);   

    groups.selectAll(".x.axis").transition().call(maplot.axis.scale(xAxis).orient("bottom"));
    groups.selectAll(".y.axis").transition().call(maplot.axis.scale(yAxis).orient("left"));


    var groupEnter = groups.enter().append("g").attr("class", "bits").style("opacity",0)
        .attr("transform", function(d)
        {
            return translateStr((uniqueGroupings[d][0]) * (groupdim[0] + normSpacing), uniqueGroupings[d][1] * (groupdim[1] + normSpacing + normTitlePadding));
        });

    groupEnter.append("svg:text").attr("class","ma title group")
        .attr("x", gTitlex)
        .attr("y", gTitley)
        .attr("text-anchor", "middle")
        .text(function(d){ return d;}).transition().style("opacity",1).delay(400);

    groups.exit().transition().style("opacity",0).duration(200).remove();

    // axes
    //groupEnter.select("g").selectAll("*").remove();
    groupEnter.each(function(block){
        var axesSelection = d3.select(this);

        var xAxes = axesSelection.selectAll("g.g.x.axis").data(xAxisPos);
            xAxes.enter().append("g").attr("class","x axis")
            
            .attr("transform", function(d)
            {
                return translateStr(d[0]*(size + spacing), (nrows-d[1])*(size) - 8);
            })
            
            .call(maplot.axis.scale(xAxis).orient("bottom"))
    .append("text")
            .attr("fill", "#000")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", "-2.0em")
            .attr("dx", "8.5em")
            .style("text-anchor", "end")
            .text("DE (log2(OFC))");

        axesSelection.selectAll("g.g.y.axis").data(yAxisPos)
            .enter().append("g").attr("class","y axis")
            .attr("transform", function(d)
            {
                return translateStr(d[0]*(size + spacing) + 5, d[1]*(size+spacing) + titlepadding);
            })
            
            .call(maplot.axis.scale(yAxis).orient("left"))
    .append("text")
            .attr("fill", "#000")
            .attr("x", 6)
            .attr("dy", "11.8em")
            .attr("dx", "8.5em")
            .style("text-anchor", "end")
            .text("Abundance (log2)");
    
    }).transition().style("opacity",1).delay(200);

    var legend = maplot.svgg.select(".legend");
    if(!legend.empty())
        legend.remove();
    maplot.svgg.append("g").attr("class","legend")
            .attr("transform", translateStr(ncolblocks * (normSpacing+groupdim[0]) + maplot.margins.left, 0))
            .call(d3.legend)


    function plot(pdata)
    {
        var cell = d3.select(this);
        cell.append("svg:rect")
                .attr("class", "frame")
                .attr("x", padding / 2)
                .attr("y", padding / 2)
                .attr("height", (size - padding))
                .attr("width", size - padding);
        var title = pdata[p];
        //title=pdata.pid[0] + "," + pdata.pid[1]
// return
        var thisx = xAxis, thisy = yAxis;
//            var ma = pdata.MA;
        var annotations = pdata.Annotations;
        var plot = cell.append("svg:g").attr("class","plotarea").attr("transform", translateStr(0, titlepadding));
        // return
//            var yvals = ma.map(function(d){return [d[dx],d[dy]]});
//            var nunique = {};
//            yvals.forEach(function(d)
//            {
//                var k = d[0]+","+d[1];
//                if(nunique[k] > 0)
//                    nunique[k]++;
//                else
//                    nunique[k] = 1;
//            });
//            
//            var orderedn = Object.keys(nunique);
//            orderedn.sort(function(a,b){return nunique[a]-nunique[b]});
//            
//            orderedn.forEach(function(d){
//                console.log([d,nunique[d]])
//            })
    plot_annotations(plot, annotations, true);


//            var points = plot.selectAll("circle")
//                    .data(ma, function(d,i){return d.Annotation+i;});

//            points.enter().append("circle")
//                    .attr("cx", function(d) {
//                        return thisx(d[dx]);
//                    })
//                    .attr("cy", function(d) {
//                        return thisy(d[dy]);
//                    })
//                    .attr("r", 1.5);

        //points.exit().remove();

        var r = xAxis.domain();
        plot.append("svg:line")
                .attr("class", "mline")
                .attr("x1", thisx(r[0]))
                .attr("x2", thisx(r[1]))
                .attr("y1", thisy(0)).attr("y2", thisy(0))
                //.attr("stroke", "black")
                .style("stroke", "red")
                .attr("stroke-width", 1)
                .attr("stroke-dasharray", "4,5");

        cell.append("text").attr("class","ma title plot").attr("x", size / 2).attr("y", titlepadding / 1.25 ).attr("text-anchor", "middle")
                .style("font-size", "15px")
                .text(function(d){ return d[p];});
        //cell.append("text").attr("x", size/2).attr("y", size/2).attr("text-anchor","middle").style("stroke","red").text(pdata[g]);
    }



    function clicked(d) {
        if (active.node() === this)
            return reset();
        active.classed("active", false);
        active = d3.select(this).classed("active", true);

        // total width and height of svg
        var width = +maplot.svg.attr("width"), height = +maplot.svg.attr("height");
        console.log(["canvas", width, height])

        // size of a group
        var dx = groupdim[0] + normSpacing;
        var dy = groupdim[1] + normSpacing + normTitlePadding;
        console.log(["group_size", dx, dy])

        var newgroupdim = getNewGroupSize(maxSize);
        var dx2 = newgroupdim[0], dy2=newgroupdim[1];
        console.log(["group", maxSize, newgroupdim])

        // origin of the group
        var x = d.pid[0] * (dx);
        var y = d.pid[1] * (dy);
        console.log(["d", d]);
        console.log(["group_origin", x, y])


        console.log(["factor", dx / width, dy / height])
        var scale = Math.min(dx2/dx, dy2/dy);
        console.log(["scale", scale]);
        //var translate = [width / 2 - scale * x, height / 2 - scale * y];
        var translate = [maplot.margins.left,maplot.margins.top];

        resizeHeight(dy2);
        console.log([scale, translate]);
        console.log(maplot.zoom.translate(translate).scale(4));
        maplot.svgg.transition().duration(750).call(maplot.zoom.translate(translate).scale(scale).event);


    }

};

function comp(a, b)
{
    if (a < b)
        return -1;
    if (a > b)
        return +1;
    return 0;
}

maplot.zoomed = function() {
    //maplot.svgg.style("stroke-width", 1.5 / d3.event.scale + "px");
    maplot.svgg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
};

window.onresize=function()
{
    if(!maplot.svg === udnefined && !maplot.svg.empty())
        maplot.svg.remove();
    maplot.initialise();
    maplot.build();
};
})();




