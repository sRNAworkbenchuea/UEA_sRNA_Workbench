// boxplots for fold change values over size classes
nscd = {};
(function () {
        nscd.margins = {"left": 100, "right": 240, "top": 80, "bottom": 80};
        nscd.dim = {"width": 800, "height": 400};
        nscd.titlePadding = 80;
        nscd.initialised = false;
        var sizeHeight=80;
        var padding = 7;
        var yAxisPadding = 15; // padding to leave room for y axis
        nscd.initialise = function ()
        {
            //if(!nscd.initialised)
            //{
            d3.select(".nscd_svg").remove();
            nscd.svg = d3.select("#nscd_div").append("svg").attr("class", "nscd_svg").attr("id", "nscd")
                    //.attr("viewBox", "0 0 800 400")
                    .attr("height", nscd.dim.height + nscd.margins.top + nscd.margins.bottom + nscd.titlePadding)
                    .attr("width", nscd.dim.width + nscd.margins.left + nscd.margins.right + nscd.titlePadding);
            nscd.svgg = nscd.svg
                    .append("g")
                    .attr("transform", "translate(" + nscd.margins.left + "," + (nscd.margins.top + nscd.titlePadding) + ")");
            nscd.initialised = true;
            //}
        };

        nscd.build = function(json, fileName)
        {
                var files = d3.nest().key(function(d){return d.File;}).entries(json);
                
                // Using only first file for testing purposes
                // TODO: extend to selection of more than one sample
             
             //console.log(fileName);
             var fileID = 0;
             for(var i = 0; i < files.length; i++)
             {
                 //console.log(files[i]);
                 if(files[i].key === fileName)
                 {
                     //alert("found");
                     fileID = i;
                 }
             }
             console.log(files[fileID]);
                var sizes = d3.nest().key(function(d){return d.Size;}).entries(files[fileID].values);
                
                // size filter
                //var sizes = options.filterSizes();
                
                
                // scales
                var xscale = d3.scale.linear().domain([0,d3.max(sizes, function(d){return d.key-1;})]).range([0,+nscd.svg.attr("width")-180]);
                var yscale = d3.scale.linear().domain([0,1]).range([sizeHeight, 0]);
                var nucol = d3.scale.category10().domain(["A", "T", "C", "G", "N"]);
                
                var xTicks = [];
                for(var i=0; i <= xscale.domain()[1]; i++)
                {
                    xTicks.push(i);
                }
                
                // draw x axis
                nscd.svg.attr("height", ((sizes.length+1)*sizeHeight) + nscd.margins.top + nscd.margins.bottom + nscd.titlePadding);
                nscd.svgg.append("g").attr("class", "x axis")
                        .attr("transform", translateStr(yAxisPadding, sizes.length*(sizeHeight+padding)))
                        .call(d3.svg.axis().scale(xscale).tickValues(xTicks).orient("bottom"));
                nscd.svgg.append("text").attr("class", "x label")
                        .attr("transform", translateStr(yAxisPadding + xscale.range()[1]/2, sizes.length*(sizeHeight+padding) + 35))
                        .text("Sequence position").attr("text-anchor", "middle").style("font-size", "14px");
                
                nscd.svgg.append("text").attr("class", "y label")
                    .attr("transform", translateStr(-30, (sizes.length*(sizeHeight + padding))/2) + " rotate(-90)")
                    .text("Proportion of sequences").attr("text-anchor", "middle").style("font-size", "14px");

                var aline = d3.svg.line()
                        .x(function(d){return xscale(d.Position);})
                        .y(function(d){return yscale(d.Count);})
                
                var area = d3.svg.area()
                    .x(function(d) { return xscale(d.Position); })
                    .y0(function(d) { return yscale(d.y0); })
                    .y1(function(d) { return yscale(d.y0 + d.y); });
                var stack = d3.layout.stack().values(function(d) {return d.values;});
                
                // per size facetting
                var sizeSelect = nscd.svgg.selectAll(".nsize")
                        .data(sizes, function(d){return d.key;});
                
                sizeSelect.transition().attr("transform", function(d,i){return translateStr(0, i*(sizeHeight+padding))});
                
                sizeSelect.exit().transition().remove();
                
                // translate facets to correct position
                var sizeEnter = sizeSelect.enter()
                        .append("g").attr("class", "nsize")
                        .attr("transform", function(d,i){return translateStr(0, i*(sizeHeight+padding))});
                
                // for each facet
                sizeEnter.each(function(d){
                    var nucs = d3.nest().key(function(dd){return dd.Nucleotide}).entries(d.values);
                    
                    var stacked = stack(nucs.map(function(n){
                        return {
                            name: n.key,
                            values: n.values.map(function(d)
                            {
                                return {Position:d.Position, y:d.Count};
                            })
                        };
                    }));
                    
                    var thisSize = d3.select(this);
                    thisSize.append("text").attr("class", "size_label")
                            .attr("transform", function(d){ return translateStr(xscale(+d.key-0.5), yscale(0));})
                            .attr("text-anchor", "start")
                            .text(d.key+"nt").style("font-size", 20);
                    
                    thisSize.append("g").attr("class", "y axis " + d.key)
                            .attr("transform", translateStr(10,0))
                            .call(d3.svg.axis().scale(yscale).tickValues([0.5,1]).orient("left"));
                    
                    var nucLines = thisSize.append("g")
                            .attr("transform", translateStr(yAxisPadding,0));
                    
                    nucLines.selectAll(".nl").data(nucs, function(dd){ return dd.key; }).enter()
                        .append("path")
                        .attr("class", function(d){return "nl "+d.key})
//                        .attr("cx", function(d){console.log(xscale(d.Size));return xscale(d.Size)})
//                        .attr("cy", function(d){return yscale(d.Count)})
                        .attr("d", function(d){ return aline(d.values)})
                        .style("stroke", function(d){ return nucol(d.key)})
//                        .style("fill", function(d){ return nucol(d.name)})
//                        .style("fill-opacity", "0.2")
                        .style("stroke-width", "1.5px");
                });
    
        };
})();


