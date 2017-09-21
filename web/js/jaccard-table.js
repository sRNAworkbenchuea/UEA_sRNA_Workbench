var jaccard = {};
jaccard.margin = {top: 20, right: 30, bottom: 80, left: 90},
jaccard.dims = [1800 + (jaccard.margin.left - jaccard.margin.right), 400 - jaccard.margin.top - jaccard.margin.bottom];
jaccard.tileSize = 60;
jaccard.tablePadding = 100; // padding between tables
jaccard.axisPadding = 5; // padding between axis labels and table
jaccard.titlePadding = 40; // padding for table titles

jaccard.initialised = false;

jaccard.intialise = function()
{
    if(!jaccard.initialised)
    {
    jaccard.svg = d3.select("#jaccard_table_div").append("svg").attr("id", "jaccard_table")
            .attr("width", jaccard.dims[0] + jaccard.margin.left + jaccard.margin.right);
            //.attr("height", jaccard.dims[1] + jaccard.margin.top + jaccard.margin.bottom)
    jaccard.svgg = jaccard.svg.append("g")
            .attr("transform", "translate(" + jaccard.margin.left + "," + jaccard.margin.top + ")");
    jaccard.initialised = true;
    }
};

jaccard.isinitialised = function()
    {
        return jaccard.initialised;
    };

jaccard.build = function(json)
{
    var y = d3.scale.ordinal()
            .rangeRoundBands([0, height], .1);

    var x = d3.scale.ordinal()
            .rangeRoundBands([0, width], .1);

    var colour = d3.scale.linear()
            .domain([0, 0.5])
            .range(["white", getThemeColour()]);
            var norms = d3.nest().key(function(d){return d.Normalisation;}).entries(json);
            
            var expanded = [];
            var unique = {};
            var i = 0;
            
            norms.forEach(function(n)
            {
               var thisUnique = {};
               n.values.forEach(function(d){
                expanded[i++] = d;
                expanded[i++] = {"Top":d.Top, "Normalisation":d.Normalisation, "Reference":d.Observed, "Observed":d.Reference, "Jaccard":d.Jaccard};
                if(!(d.Reference in thisUnique))
                    expanded[i++] = {"Top":d.Top, "Normalisation":d.Normalisation, "Reference":d.Reference, "Observed":d.Reference, "Jaccard":1};
                if(!(d.Observed in thisUnique))
                    expanded[i++] = {"Top":d.Top, "Normalisation":d.Normalisation, "Reference":d.Observed, "Observed":d.Observed, "Jaccard":1};
                thisUnique[d.Reference] = 1;
                thisUnique[d.Observed] = 1;
               });
               unique = thisUnique;
            });
            
            expanded.sort(function(a,b)
            {
                var norm = a.Normalisation.localeCompare(b.Normalisation);
                if(norm === 0)
                {
                    var ref = a.Reference.localeCompare(b.Reference);
                    if(ref === 0)
                    {
                        return a.Observed.localeCompare(b.Observed);
                    }
                    else
                    {
                        return ref;
                    }
                }
                else
                {
                    return norm;
                }
                
            });
            var normsExpanded = d3.nest().key(function(d){return d.Normalisation;}).entries(expanded);
            var uniqueFiles = Object.keys(unique).sort(function(a,b){return a.localeCompare(b);});

            var filteredNormsExpanded = options.filterArray2(normsExpanded, "key", options.normTypes);
            
            var tableSize = jaccard.tileSize*uniqueFiles.length;

            var ncols = Math.floor(jaccard.svg.attr("width") / ( tableSize + jaccard.tablePadding ));

            var nrows = Math.ceil(filteredNormsExpanded.length / ncols);
            var graphHeight = nrows*(tableSize + jaccard.tablePadding + jaccard.axisPadding + jaccard.titlePadding) + jaccard.margin.top + jaccard.margin.bottom;
            jaccard.svg.attr("height", graphHeight);
            
            var normhash = {};
            var gcol=0, grow = 0;
            filteredNormsExpanded.forEach(function(d){
                normhash[d.key] = {pos:[gcol, grow]};
                if (gcol >= (ncols - 1))
                {
                    gcol = 0;
                    grow++;
                }
                else
                {
                    gcol++;
                }
            });
            
            
            var normSelect = jaccard.svgg.selectAll("g.norm").data(filteredNormsExpanded, function(d){return d.key;});
            
            var groupTranslate = function(d){return translateStr(normhash[d.key].pos[0] * ( tableSize + jaccard.tablePadding + jaccard.axisPadding), 
                                                normhash[d.key].pos[1] * ( tableSize + jaccard.tablePadding + jaccard.axisPadding + jaccard.titlePadding)); }
            
            normSelect.transition().duration(300).attr("transform", groupTranslate );
            
            var normEnter = normSelect.enter().append("g").attr("class", function(d,i){ return "norm " + d; })
                    .attr("transform", function(d) { return groupTranslate(d); }).style("opacity", 0).transition().style("opacity",1).duration(300);
            
            normSelect.exit().transition().duration(300).style("opacity", 0).remove();
            
            normEnter.each(function(n)
            {
                var normSelect = d3.select(this);
                var gridSize = jaccard.tileSize;
                var xLabels = normSelect.append("g")
                        .attr("class", "x label")
                        //.attr("transform", translateStr(-400,0));

                var reverseUniqueFiles = uniqueFiles.slice();
                reverseUniqueFiles.reverse();
                console.log(uniqueFiles);
                xLabels.append("g").attr("transform",translateStr(0,jaccard.titlePadding))
                    .selectAll(".xlab").data(reverseUniqueFiles)
                    .enter().append("text").text(function (d){ return d; })
                    .attr("class", "xlab")
                    .attr("fill", "rgb(0,0,0)")
                    //.attr("transform", function(d, i) { return translateStr((i+1)*jaccard.tileSize + (jaccard.tileSize/2), tableSize); })
                    //.attr("x", -(tableSize + jaccard.tablePadding))
                    .attr("text-anchor", "end")
                    .attr("transform", function(d, i) { return "rotate(-90, "+ tableSize/2 + ", " + tableSize/2 + ") " +  translateStr(0, (i)*gridSize + (gridSize/2)); })
                    .attr("x", -jaccard.axisPadding);


                var yLabels = normSelect.selectAll(".ylab").data(uniqueFiles)
                    .enter().append("text").text(function (d){ return d; })
                    .attr("class", "ylab")
                    .attr("fill", "rgb(0,0,0)")
                    .attr("y", function(d, i) { return i*gridSize + (gridSize/2) + jaccard.titlePadding; })
                    //.attr("x", -jaccard.axisPadding)
                    .attr("text-anchor", "end");
                
                var title = normSelect.append("text").attr("class", "table title").attr("fill", "rgb(0,0,0)").attr("x", jaccard.axisPadding + (tableSize/2)).attr("y", jaccard.titlePadding/2)
                        .text(n.key).attr("text-anchor", "middle");
                
                var table = normSelect.append("g").attr("class", "table")
                    .attr("transform", translateStr(jaccard.axisPadding, jaccard.titlePadding))
            
                table.append("line").attr("x1",0).attr("y1",tableSize).attr("x2", tableSize).attr("y2", 0)
                        .style("stroke","lightgrey").style("stroke-width", 2);

                        var jFormat = function(jaccard)
                        {

                            if(jaccard.Observed === jaccard.Reference)
                            {
                                return "";
                            }
                            var j = jaccard.Jaccard;
                            if(j === 1 || j === 0)
                            {
                                return j+".00";
                            }

                            return /(\d\.\d{2})/.exec(""+j)[1];
                        };
                

                var heatmap = table.selectAll(".heat")
                        .data(n.values)
                        .enter().append("g").attr("class", "heat jaccard")
                        .attr("transform", function(d, i) { 
                            return translateStr(uniqueFiles.indexOf(d.Reference)*gridSize, tableSize-((1+uniqueFiles.indexOf(d.Observed))*gridSize));
                        })
                        .each(function(j){

                            var tile = d3.select(this);
                            tile.append("rect")
    //                       .attr("x", function(d, i) { return (uniqueFiles.indexOf(d.Reference)*gridSize);})
    //                       .attr("y", function(d, i) { return (jaccard.dims[1]-((1+uniqueFiles.indexOf(d.Observed))*gridSize));})
                           .attr("class", "tile")
                           .attr("width", gridSize)
                           .attr("height", gridSize)
                           .style("fill", function() { return (j.Jaccard === 1 ? "white" : colour(j.Jaccard));})
                           .style("fill-opacity", function() { return (j.Jaccard === 1 ? 0 : 1);})
                           .style("stroke", "black")
                           .style("stroke-width", 3);

                           tile.append("text").text(jFormat(j))
                                   .attr("x", gridSize/2)
                                   .attr("y", gridSize/2)
                                   .attr("text-anchor", "middle");

                        });
                });

};

window.onresize = function ()
{
    if (!jaccard.svg === undefined){
        if(!jaccard.svg.empty())
            jaccard.svg.remove();
        jaccard.initialise();
        jaccard.build();
    }
};