function MAPlotter(size, padding, expand, xAxis, yAxis){
    this.size = size;
    this.padding = padding;
    this.expand = expand;
    this.pex = this.padding*this.expand;
    this.xAxis = xAxis;
    this.yAxis = yAxis;
    this.axis = d3.svg.axis().ticks(4);
    
    this.maplot = function(selection, data)
    {
        selection = d3.select(selection);
        //var cell = selection;
        var cell = selection.append("svg:rect")
                .attr("class", "frame")
                .attr("x", 0)
                .attr("y", 0)
                .attr("height", size+this.pex*2)
                .attr("width", size+this.pex*2)
                .style("fill", "white").style("stroke", "black")
        
        var thisx = xAxis, thisy = yAxis;
        var plot = selection.append("svg:g").attr("class", "plotarea").attr("transform", translateStr(this.pex, this.pex));
        
//        var axesSelection = selection.append("g");
        selection.append("g").attr("class", "x axis")
                .attr("transform",  translateStr(this.pex,size+(this.pex*2)))
                .call(this.axis.scale(xAxis).orient("bottom"));

        selection.append("g").attr("class", "y axis")
                .attr("transform", translateStr(0, this.pex))
                .call(this.axis.scale(yAxis).orient("left"));
        
        var r = xAxis.domain();

        
        var t = this;
        var parea = plot
           // .attr("transform", translateStr(padding, 0))
                //.attr("class")
        parea.append("svg:line")
                .attr("class", "mline")
                .attr("x1", thisx(r[0]))
                .attr("x2", thisx(r[1]))
                .attr("y1", thisy(0)).attr("y2", thisy(0))
                .style("stroke", "red")
                .attr("stroke-width", 1)
        //.attr("stroke-dasharray", "4,5");
        parea.append("svg:line")
                .attr("class", "mline")
                .attr("x1", thisx(r[0]))
                .attr("x2", thisx(r[1]))
                .attr("y1", thisy(1)).attr("y2", thisy(1))
                .style("stroke", "red")
                .attr("stroke-width", 1)
                .attr("stroke-dasharray", "4,5");
        parea.append("svg:line")
                .attr("class", "mline")
                .attr("x1", thisx(r[0]))
                .attr("x2", thisx(r[1]))
                .attr("y1", thisy(-1)).attr("y2", thisy(-1))
                .style("stroke", "red")
                .attr("stroke-width", 1)
                .attr("stroke-dasharray", "4,5");
        parea.call(function(d){t.plot_points(this,data)});
    }
    
    this.plot_points = function (selectThis,a)
    {

        var points = selectThis
                .selectAll("circle");
        var newPoints = points.data(a);
        var circles = newPoints.enter().append("circle");
        var fcd = function(d){
            if(d.d==="U"){
                return +d.M
            }else if(d.d==="D"){
                return 0-d.M
            }else {return 0} 
        }

        circles
                .attr("cx", function (d) {
                    return xAxis(d.A);
                })
                .attr("cy", function (d) {
                    return yAxis(fcd(d));
                })
                .attr("r", 1)
                //.attr("fill", colours(a.Annotation))
    }; 
}


