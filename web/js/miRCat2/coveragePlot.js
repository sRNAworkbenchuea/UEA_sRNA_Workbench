/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function buildCoverage(path, callback, heading)
{
    //var r = $.Deferred();
    
//    if(!d3.select("#coveragePlot_div").empty())
//    {
//        d3.select("#coveragePlot_div").selectAll().remove();
//
//    }
    
    
        
        
    var svg = d3.select("#coveragePlot_div").append("svg").attr("width", 960).attr("height", 500).attr("id","coverageSVG"),
            margin = {top: 80, right: 20, bottom: 80, left: 80},
    width = +svg.attr("width") - margin.left - margin.right,
            height = +svg.attr("height") - margin.top - margin.bottom,
            g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    //var parseTime = d3.timeParse("%d-%b-%y");

    var x = d3.scaleLinear()
            .rangeRound([0, width]);

    var y = d3.scaleLinear()
            .rangeRound([height, 0]);

    var line = d3.line()
            .x(function (d) {
                return x(d.hairpincoord);
            })
            .y(function (d) {
                return y(d.shortreadabundance);
            });

    d3.tsv(path, function (d) {
        d.hairpincoord = +d.hairpincoord;
        d.shortreadabundance = +d.shortreadabundance;
        return d;
    }, function (error, data) {
        if (error)
            throw error;

        x.domain(d3.extent(data, function (d) {
            return d.hairpincoord;
        }));
        y.domain(d3.extent(data, function (d) {
            return d.shortreadabundance;
        }));

        g.append("g")
                .attr("class", "axis axis--x")
                .attr("transform", "translate(0," + height + ")")
                .call(d3.axisBottom(x))
                .append("text")
                .attr("fill", "#000")
                .attr("x", width/2)
                .attr("dy", "2.3em")
                .style("text-anchor", "middle")
                .text("Position");

        g.append("g")
                .attr("class", "axis axis--y")
                .call(d3.axisLeft(y))
                .append("text")
                .attr("fill", "#000")
                .attr("transform", "rotate(-90)")
                .attr("y", 6)
                .attr("dy", "-3.0em")
                .style("text-anchor", "end")
                .text("Abundance");

        g.append("path")
                .datum(data)
                .attr("class", "coverageline")
                .attr("d", line);
        
        g.append("text")
        .attr("x", (width / 2))             
        .attr("y", 0 - (margin.top / 2))
        .attr("text-anchor", "middle")  
        .style("font-size", "16px") 
        .style("text-decoration", "underline")  
        .text(heading);
            
    if(callback !== null && typeof callback !== 'undefined')
        callback();

    });
    
    //r.resolve();
    
    //return r;
}
/*
 c_plot = {};
 (function () {
 var svg = d3.select("svg"),
 margin = {top: 20, right: 20, bottom: 30, left: 50},
 width = +svg.attr("width") - margin.left - margin.right,
 height = +svg.attr("height") - margin.top - margin.bottom,
 g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
 
 var parseTime = d3.timeParse("%d-%b-%y");
 
 var x = d3.scaleTime()
 .rangeRound([0, width]);
 
 var y = d3.scaleLinear()
 .rangeRound([height, 0]);
 
 var line = d3.line()
 .x(function(d) { return x(d.date); })
 .y(function(d) { return y(d.close); });
 
 d3.tsv("../../TSV/testCoverage.tsv", function(d) {
 d.date = parseTime(d.date);
 d.close = +d.close;
 return d;
 }, function(error, data) {
 if (error) throw error;
 
 x.domain(d3.extent(data, function(d) { return d.date; }));
 y.domain(d3.extent(data, function(d) { return d.close; }));
 
 g.append("g")
 .attr("class", "axis axis--x")
 .attr("transform", "translate(0," + height + ")")
 .call(d3.axisBottom(x));
 
 g.append("g")
 .attr("class", "axis axis--y")
 .call(d3.axisLeft(y))
 .append("text")
 .attr("fill", "#000")
 .attr("transform", "rotate(-90)")
 .attr("y", 6)
 .attr("dy", "0.71em")
 .style("text-anchor", "end")
 .text("Price ($)");
 
 g.append("path")
 .datum(data)
 .attr("class", "line")
 .attr("d", line);
 });
 })();
 */