var BoxplotsByAnnotation = function(wrapper, keys, options){
    /**
     * Plots a facet of boxplots per annotation
     * Each facet contains a boxplots group for each normalisation
     * found in the data. The key xKey can be used to extract the x values
     * for each individual boxplot
     * 
     * keys are variable names for each level of the graph
     * keys.chart , keys.group , keys.box , keys.selection
     */
    this.margins = {"left": 100, "right": 40, "top": 40, "bottom": 1140};
    this.dim = {"width": 800, "height": 250};
    this.titlePadding = 40;
    this.labelPadding=40;
    
    this.bpOptions = this.applyDefaults(options || {});
    
    //var options = wbOptions; // instance of WbOptions
    
    this.keyvars = keys;
    this.chartvar = "Pair";
    this.groupvar = "Normalisation";
    this.boxvar = "Size";
    this.selectionvar = "Annotation";
    this.wrapper = wrapper;
    this.init();
    };
    
    BoxplotsByAnnotation.prototype = {
        init: function(){    
            // remove any previous boxplot chart
            if(!d3.select(".boxplot").empty())
                d3.select(".boxplot").remove();
            
            // Increase width of SVG if the given maximum graph width is wider.
            if(this.bpOptions.maxGraphWidth > this.dim.width)
                this.dim.width=this.bpOptions.maxGraphWidth + 50;
            this.svg = d3.select(this.wrapper).append("svg").attr("class", "boxplot")
                        .attr("height", this.dim.height + this.margins.top + this.margins.bottom + this.titlePadding)
                        .attr("width", this.dim.width + this.margins.left + this.margins.right);
            this.svgg = this.svg
                    .append("g")
                    .attr("transform", "translate(" + this.margins.left + "," + (this.margins.top + this.titlePadding) + ")");
        },   
        
        build: function(data, selectionValue, lineData, showLabels, showLegend, yintercept){
            // accepts nested data, grouped by the specified keys
            // selectionValue is the value for the key in keys.selection
            
            this.init();
            
            var padding = 40;
            var cv = this.keyvars.chart;
            var sv = this.keyvars.selection;
            var gv = this.keyvars.group;
            var bv = this.keyvars.box;
             var gwidth = this.dim.width;
            var selectedCharts = data;
            if(sv !== undefined){
                selectedCharts = options.filterArray2(data, sv, [selectionValue])
            }
            
            data = d3.nest()
                .key(function(d){return d[cv];})       // Per chart
                //.key(function(d){return d[sv];})    // selection
                .key(function(d){return d[gv];}) // grouped
                .entries(selectedCharts);
        
            // compute lines line graph overlay for e.g. the series fold change pattern boxplots
            var lineNest = undefined;            
            if(lineData !== undefined){
                    lineNest = d3.nest().key(function(d){return d[cv];}).key(function(d){return d["SampleSeries"];}).entries(lineData);
            }
        
            var titlePadding = this.titlePadding;
            var labelPadding = this.labelPadding;
            var bpOps = this.bpOptions;
            if(showLabels){showLabels=[true]}else{showLabels=[]}
            var bp = new Boxplot(selectionValue, this.bpOptions, showLabels,[showLegend],[], this.bpOptions.drawXaxis);
            this.svg.attr("height", ((data.length+1)*(bpOps.graphHeight+this.labelPadding+this.titlePadding)) + this.margins.top + this.margins.bottom + this.titlePadding+padding);
            
            var graphSelect = this.svgg.selectAll("g.graph").data(data, function (d) {return d.key;});
            var svgg = this.svgg;
            // Facet update
            graphSelect.transition().attr("transform", function(d,i){
                return translateStr(0, i*(bpOps.graphHeight + padding + titlePadding));
            }).each(function(d){
                // find the window currently in use
                
//                var norms = (selectedCharts === null) ? [] : selectedCharts.values;
//                console.log(norms);
                
                var thisGraph = d3.select(this);
                bp.addBoxplotGroup(thisGraph, d.values, bv, 0);
            });
            
            graphSelect.exit().transition().duration(this.bpOptions.transitionDuration).remove();
            
            graphSelect.enter()
                    .append("g").attr("class", function(d){return "graph" + d.key; })
                    .attr("transform", function(d,i){
                        return translateStr(0, i*(bpOps.graphHeight + padding + titlePadding + labelPadding ));
            })          
           
            
            // each facet
            .each(function(chartdata){
                var thisGraph = d3.select(this);

                thisGraph.append("text")
                    .attr("class","annotation title")
                    .attr("x", 0)
                    .attr("y", -30)
                    .text(chartdata.key);
                // data keyed by normalisation -> filename for this window
                //var norms = find(chartdata.values, "key", selectionValue).values;
                //var N = getN(thisWindow, sizeHeight);
               
                var linesForChart = undefined;
                
                if(lineNest !== undefined){
                    var linesForChart = find(lineNest, "key", chartdata.key);

                    //lineNest = d3.nest().key(function(d){return d[cv];}).key(function(d){return d["SampleSeries"];}).entries(lineData);
                }
                
                bp.addBoxplotGroup(thisGraph, chartdata.values, bv, 0, linesForChart, yintercept);

//                // add legend
                if(showLegend){
                    var legend = svgg.selectAll("g.legend").data([showLegend], function(d){return d;});
                    legend.transition().call(d3.legend);

                    var legendEnter = legend.enter().append("g").attr("class", "legend")
                            .attr("transform", translateStr(gwidth,0))
                            .call(d3.legend);  

                    legend.exit().transition().remove();
                }



            });
        },
        defaults : {
            maxGraphWidth: 700,
            maxBoxWidth: 30,
            graphHeight: 250,
            spacing: 0.25,
            transitionDuration: 400,
            drawXaxis: true
        },
        applyDefaults: function(options) {
          for (var k in this.defaults) {
            if (!options.hasOwnProperty(k)) {
              options[k] = this.defaults[k];
            }
          }
          return options;
        }
    }

