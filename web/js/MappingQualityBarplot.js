var MappingQualityBarplot = function(wrapper)
{
    this.wrapper = wrapper;
    this.margins = {"left": 200, "right": 200, "top": 40, "bottom": 40};
    this.dim = {"width": 800, "height": 300};
    this.init();
}
MappingQualityBarplot.prototype = {
    init: function(){
        if(!d3.select(".mq_bars").empty())
            d3.select(".mq_bars").remove();

        this.svg = d3.select(this.wrapper).append("svg").attr("class", "mq_bars")
             .attr("height", this.dim.height + this.margins.top + this.margins.bottom)
             .attr("width", this.dim.width + this.margins.left + this.margins.right);
        this.svgg = this.svg
                .append("g")
                .attr("transform", "translate(" + this.margins.left + "," + (this.margins.top) + ")")
//        this.svgg.append("g").attr("class", "");
//        this.svgg.append("g").attr("class", "mapped");
        this.svgg.append("g").attr("class", "x axis").attr("transform", "translate(0," + this.dim.height + ")")
        this.svgg.append("g").attr("class", "y axis")
        this.svgg.append("g").attr("class", "legend")
    },

    /*
     * Accepts Size class distribution JSON data
     */
    build: function(data, annotation, type, percentage){
        var tdur = 400;
        var mq = this;
        var normalisation="RAW";
        var all_annotation="All";
        var unmapped = "none";
        var mapped = "Mapped";
        var typeComp = type === "Complexity";
        
        // Percentage and Complexity are mutually exclusive
        if(percentage && typeComp)
        {
            percentage = false;
        }
        
        var filtered = [];
        data.forEach(function(d){
            if(d.Normalisation === normalisation)
            {
                filtered.push(d);
            }
        })

        var byFile = d3.nest().key(function(d){return d.Filename}).entries(filtered);
        var byAnnot = d3.nest().key(function(d){return d.Annotation}).entries(filtered);
        
        var uniqueAnnots = [unmapped];
        byAnnot.forEach(function(d){
            if(d.key !== unmapped && d.key !== mapped && d.key !== all_annotation){
                uniqueAnnots.push(d.key);
            }
        })
        uniqueAnnots.push(mapped)
        
        var fileTotals = [];
        var maxValue = 0;
        byFile.forEach(function(d){
            var file = d.key;
            var total = 0;
            var annotationTotals = [];
            uniqueAnnots.forEach(function(a){
                var thisAnnot = findAll2(d.values, "Annotation", a);
                var sizeSum = {Redundant:0, Nonredundant:0};
                thisAnnot.forEach(function(x){
                    sizeSum.Redundant += x.Redundant;
                    sizeSum.Nonredundant += x.Nonredundant;
                })
                if(typeComp){
                    total = sizeSum.Nonredundant/sizeSum.Redundant
                }
                else{
                    total += sizeSum[type]
                }
                annotationTotals.unshift({Annotation:a, Count:total});
                maxValue = d3.max([maxValue, total])
            })
            
            fileTotals.push({Filename:file, Annotations:annotationTotals, Total:total})
        })
        
        var width = mq.dim.width;
        var height = mq.dim.height;
        
        var annot_colours = getWbColourScale(uniqueAnnots.length).domain(uniqueAnnots); 
        console.log(annot_colours.range())
        var y = d3.scale.ordinal()
        .rangeRoundBands([0, height], .1);

        var x = d3.scale.linear()
                .range([0, width]);
        
        y.domain(fileTotals.map(function(d){return d.Filename}))
        x.domain([0, (percentage) ? 1 : maxValue]);
        
        // unmapped bars, go under mapped bars for stacking.
        var filebar = mq.svgg.selectAll(".filebar").data(fileTotals, function (d) {return d.Filename});
        
        filebar.transition().attr("class", "filebar").each(function(d){
            var i = 0;
            var numAnnots = d.Annotations.length;
            var h = (typeComp) ? y.rangeBand()/numAnnots : y.rangeBand();
            
            d3.select(this).selectAll("rect")
                    .data(d.Annotations, function(a){return a.Annotation})
                    .transition().duration(tdur)
                    .attr("width", function (dd) {return (percentage) ? x(dd.Count / d.Total) : x(dd.Count)})
                    .attr("y", function(dd,i){return (typeComp) ? y(d.Filename)+h*i : y(d.Filename)})
                    .attr("height", h)
                    
        });
        
        filebar.enter().append("g").attr("class", "filebar").each(function(d){
            var annotRect = d3.select(this).selectAll("rect")
                    .data(d.Annotations, function(a){return a.Annotation})
            annotRect.enter().append("rect")
                    .attr("y", y(d.Filename))
                    .attr("height", y.rangeBand())
                    .attr("width", function (dd) {return (percentage) ? x(dd.Count / d.Total) : x(dd.Count)})
                    .attr("data-legend", function(dd){return dd.Annotation})
                    .style("fill", function(dd){return annot_colours(dd.Annotation)})
                    .style("stroke", "black")
        })
        
        this.svgg.select(".x.axis")
                .transition().duration(tdur)
                .call( d3.svg.axis().scale(x).orient("bottom"));

        this.svgg.select(".y.axis")
                .transition().duration(tdur)
                .call(d3.svg.axis().scale(y).orient("left"));
        
        var legend = this.svgg.select(".legend")
        if (!legend.empty())
            legend.remove();
        this.svgg.append("g").attr("class", "legend").attr("transform", translateStr(width+50, 0)).call(d3.legend);
        
    }


}


