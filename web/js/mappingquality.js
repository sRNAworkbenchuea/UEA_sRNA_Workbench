/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var margin = {top: 20, right: 20, bottom: 35, left: 80},
width = 800 - margin.left - margin.right,
        height = 300 - margin.top - margin.bottom;

var formatPercent = d3.format(".0%");
var formatInt = d3.format(",.0f");
var formatFloat = d3.format(".1f");

var durations = 600;

var y = d3.scale.ordinal()
        .rangeRoundBands([0, height], .1);

var x = d3.scale.linear()
        .range([0, width]);

var xAxis, yAxis;

var unmapped, mapped;

var svg;

var axisText = {"Redundant": "redundant count", "Nonredundant": "non-redundant count",
    "PercentRedundant": "percentage mapped (redundant)", "PercentNonredundant": "percentage mapped (non-redundant)",
    "Complexity": "count complexity (non-redundant/redundant)"};

//var type = "Redundant";

function findCounts(data, M)
{
    return find(data.Statuses, "Status", M);
}

function buildMappingQualityChart(defaultType)
{
    console.log("firstbuild");
    svg = d3.select("#mapping_quality").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    d3.json("../json/total.json", function (error, json) {
        


         xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");

         yAxis = d3.svg.axis()
                .scale(y)
                .orient("left");

        samples = json.Normalisations[0].Samples;
        samples.forEach(function (sample) {
            var dm = find(sample.Statuses, "Status", "Mapped");
            var du = find(sample.Statuses, "Status", "Unmapped");

            if (du === null)
            {
                sample.Statuses[sample.Statuses.length] = {"Status": "Unmapped", "Counts": {"Redundant": 0, "Nonredundant": 0, "Complexity": 0}};
                du = find(sample.Statuses, "Status", "Unmapped");

            }
            if (dm === null)
            {
                sample.Statuses[sample.Statuses.length] = {"Status": "Mapped", "Counts": {"Redundant": 0, "Nonredundant": 0, "Complexity": 0}};
                vm = find(sample.Statuses, "Status", "Mapped");

            }

            
            dm.PercentRedundant = (dm.Counts.Redundant / (dm.Counts.Redundant + du.Counts.Redundant));
            du.PercentRedundant = (dm.Counts.Redundant / (dm.Counts.Redundant + du.Counts.Redundant));
            dm.PercentNonredundant = (dm.Counts.Nonredundant / (dm.Counts.Nonredundant + du.Counts.Nonredundant));
            du.PercentNonredundant = (du.Counts.Nonredundant / (dm.Counts.Nonredundant + du.Counts.Nonredundant));

            dm.Redundant = dm.Counts.Redundant;
            dm.Nonredundant = dm.Counts.Nonredundant;
            du.Redundant = dm.Counts.Redundant + du.Counts.Redundant;
            du.Nonredundant = dm.Counts.Nonredundant + du.Counts.Nonredundant;

            dm.Complexity = dm.Counts.Complexity;
            //du.Complexity = dm.Counts.Complexity + du.Counts.Complexity;
            du.Complexity = du.Counts.Complexity;

        });
        var type = defaultType;

        y.domain(samples.map(function (d) {
            return d.Sample;
        }));
        x.domain([0, d3.max(samples, function (d) {
                return d3.max(d.Statuses, function (e) {
                    return e[type];
                });
            })]);

        unmapped = svg.append("g").attr("class", "unmapped")
                .selectAll("rect").data(samples, function (d) {
            return d.Sample
        });
        unmapped.enter()
                .append("rect")
                .attr("class", "bar under")
                .attr("y", function (d) {
                    return y(d.Sample);
                })
                .attr("height", function (d) {
                    return y.rangeBand();
                })
                .attr("width", function (d) {
                    var r = findCounts(d, "Unmapped")[type];
                    console.log(r);
                    return x(r);
                }).attr("data-legend", "Unmapped");

        mapped = svg.append("g").attr("class", "mapped")
                .selectAll("rect").data(samples, function (d) {
            return d.Sample
        });

        mapped.enter()
                .append("rect")
                .attr("class", "bar over")
                .attr("y", function (d) {
                    return y(d.Sample);
                })
                .attr("height", function (d) {
                    return y.rangeBand();
                })
                .attr("width", function (d) {
                    var r = findCounts(d, "Mapped")[type];
                    console.log(r);
                    return x(r);
                }).attr("data-legend", "Mapped");
                
        svg.append("g").attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);
        
        svg.append("text").attr("class", "x text")
                .attr("transform", "translate(" + width / 2 + "," + (height + 31) + ")")
                .text(axisText[type]).style("text-anchor", "middle");

        svg.append("g").attr("class", "y axis")
                .call(yAxis);
        
        console.log("fb end");
        
        // add legend
        d3.select("#mapping_quality").select("svg").append("g").attr("class", "legend")
                .attr("transform", "translate(60,"+(height+margin.top+margin.bottom)+")")
                .attr("data-style-padding", "5")
                .style("font-size","20px")
                .call(d3.legend);
        console.log("fb end end");
    });

}
  function updateMappingQuality(type) {
      console.log("updating");
            var thisFormat;
            switch (type)
            {
                case "Redundant":
                case "Nonredundant":
                    thisFormat = formatInt;
                    break;
                case "PercentRedundant":
                case "PercentNonredundant":
                    thisFormat = formatPercent;
                    break;
                default:
                    thisFormat = formatFloat;
            }
            xAxis.tickFormat(thisFormat);



            x.domain([0, d3.max(samples, function (d) {
                    return d3.max(d.Statuses, function (e) {
                        return e[type];
                    });
                })]);

            svg.transition().duration(durations).select("g.x.axis").call(xAxis);


            var umt = 
            unmapped.transition().duration(durations);
            
            umt.attr("width", function (d) {
   
                    return x(findCounts(d, "Unmapped")[type]);
            });
            


            //unmapped.exit().remove();

            var mt = mapped.transition().duration(durations);
            mt.attr("width", function (d) {
                return x(findCounts(d, "Mapped")[type]);
            });
            
            if(type === "Complexity")
            {
                umt.attr("height", function(d){ return y.rangeBand()/2;})
                .attr("y", function(d){return y(d.Sample) + y.rangeBand()/2;});
                mt.attr("height", function(d){ return y.rangeBand()/2;})          
            }
            else
            {
                umt.attr("height", function(d){ return y.rangeBand()})
                .attr("y", function(d){return y(d.Sample);});
                mt.attr("height", function(d){ return y.rangeBand();}) 
            }


            svg.select(".x.text").transition().duration(durations).attr("opacity", 0.1).remove();
            svg.append("text").attr("class", "x text")
                    .attr("transform", "translate(" + width / 2 + "," + (height + 31) + ")")
                    .text(axisText[type]).style("text-anchor", "middle");

            //mapped.exit().remove;


        }



