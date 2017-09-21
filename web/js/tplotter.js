
var tooltip_padding = 5;
var line_height = 20;


var the_tplot = null;

function rotateVector(x, y, angle)
{
    var new_x = x * Math.cos(angle) + y * -Math.sin(angle);
    var new_y = x * Math.sin(angle) + y * Math.cos(angle);
    return [new_x, new_y];
}

function toRadians(angle) {
    return (angle * Math.PI) / 180.0;
}




function tplot(canvasID)
{
    this.data = null;
    this.canvasID = canvasID;
 

    this.visualise = function ()
    {

        window.onresize = $.proxy(this.visualise, this);
        var svgContainerElement = document.getElementById(this.canvasID);
        /*while (svgContainerElement.firstChild) {
         svgContainerElement.removeChild(svgContainerElement.firstChild);
         }*/



        var svgContainer = d3.select("#" + this.canvasID);
        var w = svgContainerElement.getBoundingClientRect().width;
        var fontSize = "6pt";//(w * 12 / 500) + "pt";
        var h = svgContainerElement.getBoundingClientRect().height;
        var hPadding = 60;
        var yPadding = 32;
        var xRange = [this.data["axis"]["xmin"], this.data["axis"]["xmax"]];// getRange('x');

        var yRange = getRange(this.data, 'y');//[0, getRange('y')[1]];


        var legendHeight = line_height * this.data["legend"].length + tooltip_padding;
        var legendTop = svgContainerElement.getBoundingClientRect().height / 2 - legendHeight / 2;
        var legend = svgContainer.selectAll(".legend").data(this.data["legend"]);
        legend.enter().append("polygon");
        legend
                .attr("fill", function (d) {
                    return d['color'];
                })
                .attr("class", "legend")
                .attr("points", poly)
                .attr("transform", function (d, i) {
                    return "translate(" + (svgContainerElement.getBoundingClientRect().width - (tooltip_padding * 2) - line_height / 4) + "," + (legendTop + (i + 1) * line_height) + ")";
                })
                .attr("stroke", "black");
        legend.exit().remove();



        var legendtext = svgContainer.selectAll(".legendtext").data(this.data["legend"]);
        legendtext.enter().append("text");
        legendtext
                .attr("class", "legendtext")
                .attr("x", svgContainerElement.getBoundingClientRect().width - (tooltip_padding * 2) - line_height / 2 - tooltip_padding)
                .attr("y", function (d, i) {
                    return legendTop + (i + 1) * line_height;
                })
                .attr("text-anchor", "end")
                .style("alignment-baseline", "central")
                .style("font-size", fontSize)
                .style("font-family", "Arial,Helvetica")

                .text(function (d) {
                    return d["key"];
                });
        legendtext.exit().remove();

        // get the largest legend key box
        var legendkeys = document.getElementsByClassName("legendtext");
        var maxX = 0;
        for (var i = 0; i < legendkeys.length; i++)
        {
            var textwidth = legendkeys[i].getBBox().width;

            if (textwidth > maxX)
            {
                maxX = textwidth;
            }
        }
        var legendboxW = maxX + tooltip_padding * 3 + line_height / 2;
        var legendbox = svgContainer.selectAll(".legendbox").data([0]);
        legendbox.enter().append("rect");
        legendbox
                .attr("id", "legendbox")
                .attr("class", "legendbox")
                .attr("x", svgContainerElement.getBoundingClientRect().width - tooltip_padding * 2 - (line_height / 2) - tooltip_padding - maxX - tooltip_padding)
                .attr("y", legendTop + tooltip_padding / 2)
                .attr("width", legendboxW)
                .attr("height", legendHeight)
                .style("fill", "white")
                .style("stroke", "black");
        legendbox.exit().remove();

        var right = svgContainerElement.getBoundingClientRect().width - tooltip_padding * 2 - (line_height / 2) - tooltip_padding - maxX - tooltip_padding;

        //Create the Scale we will use for the Axis

        var yAxisScale = d3.scale.linear()
                // .base(Math.E)
                .domain(yRange)
                .range([h - yPadding, yPadding]);




        var yAxis = d3.svg.axis()
                .scale(yAxisScale)
                .orient("left")
                .ticks(5, ",.1s");


        var y = svgContainer.selectAll(".yaxis").data([0]);
        // y.transition()
        y.call(yAxis);
        //.each("end", myCallback);
        y.enter().append("g")
                .call(yAxis)
                .attr("class", "yaxis") //Assign "axis" class
                .attr("id", "yaxis") //Assign "axis" class
                .attr("transform", "translate(" + hPadding + ", 0)")
                .style("font-size", fontSize)
                .style("font-family", "Arial,Helvetica")
                .style("fill", "none")
                .style("stroke", "black")
                .style("shape-rendering", "crispEdges");
        y.selectAll("text").style("fill", "black")
                .style("stroke", "none");
        y.exit().remove();




        // calculate the how much to shift the y-axis along the x-axis
        var maxw = 0;
        var y = d3.select("#tplot_svg").selectAll(".yaxis");
        y.selectAll("text").each(function () {
            if (this.getBBox().width > maxw)
                maxw = this.getBBox().width;
        });
        var yaxis_shift = maxw + yPadding;
        y.attr("transform", "translate(" + yaxis_shift + ",0)");

        var xAxisScale = d3.scale.linear()
                .domain(xRange)
                .range([yaxis_shift, right - tooltip_padding]);

        //Create the Axis
        var xAxis = d3.svg.axis()
                .scale(xAxisScale)
                .orient("bottom")
                .ticks(3);

        var x = svgContainer.selectAll(".xaxis").data([0]);
        // x.transition()
        x.enter().append("g");
        x.call(xAxis)
                .attr("class", "xaxis") //Assign "axis" class
                .attr("id", "xaxis") //Assign "axis" class
                .attr("transform", "translate(0," + (h - yPadding) + ")")
                .style("font-size", fontSize)
                .style("font-family", "Arial,Helvetica")
                .style("fill", "none")
                .style("stroke", "black")
                .style("shape-rendering", "crispEdges");
        x.selectAll("text").style("fill", "black")
                .style("stroke", "none");
        x.exit().remove();




        var lines = svgContainer.selectAll(".lines").data(this.data["points"]);
        lines.transition()
                .attr("x1", function (d) {
                    return xAxisScale(d['x']);
                })
                .attr("x2", function (d) {
                    return xAxisScale(d['x']);
                })
                .attr("y1", h - yPadding)
                .attr("y2", function (d) {
                    return yAxisScale(d['y']);
                });
        lines.enter().append("line")
                .attr("class", "lines") //Assign "axis" class

                .attr("x1", function (d) {
                    return xAxisScale(d['x']);
                })
                .attr("x2", function (d) {
                    return xAxisScale(d['x']);
                })
                .attr("y1", h - yPadding)
                .attr("y2", function (d) {
                    return yAxisScale(d['y']);
                })
                .style("fill", "none")
                .style("stroke", "black")
                .style("shape-rendering", "crispEdges");
        lines.exit().remove();

        var points = svgContainer.selectAll(".points").data(this.data["points"]);
        points.on("mousemove", tooltipMouseMove);
        points.transition()

                .attr("transform", function (d) {
                    return "translate(" + xAxisScale(d['x']) + "," + yAxisScale(d['y']) + ")";
                })
                .style("fill", function (d) {
                    return d['color'];
                });

        points.enter().append("polygon")
                .attr("class", "points")
                .attr("points", poly)
                .attr("transform", function (d) {
                    return "translate(" + xAxisScale(d['x']) + "," + yAxisScale(d['y']) + ")";
                })
                .style("stroke", "black")
                .style("stroke-width", "1px")
                .style("fill", function (d) {
                    return d['color'];
                })

                .on("mousemove", tooltipMouseMove)
                .on("mouseenter", function (d) {
                    focus.style("display", "block");


                    focustext.style("display", "block");

                })
                .on("mouseleave", function (d) {

                    var tooltipelement = document.getElementById("tooltip");
                    // reset its content (remove multiple tspan elements from previous tip)
                    while (tooltipelement.firstChild) {
                        tooltipelement.removeChild(tooltipelement.firstChild);
                    }
                    focus.style("display", "none");
                    focustext.style("display", "none");


                });
        points.exit().remove();

        function poly(d)
        {
            var nPoints = d['nPoints'];

            var angle = toRadians(360.0 / nPoints);
            var r = d['radius'];
            var output = "";
            for (var i = 0; i < nPoints; i++)
            {
                var pos = rotateVector(0, r, angle * i);
                output += pos[0] + "," + pos[1] + " ";
            }
            return output;
        }
        function tooltipMouseMove(d)
        {
            // define the elipses string
            var elipses = "...";
            // get the svg width and height
            var svgW = svgContainerElement.getBoundingClientRect().width;
            var svgH = svgContainerElement.getBoundingClientRect().height;
            // get the tooltip element from the DOM
            var tooltipelement = document.getElementById("tooltip");
            // reset its content (remove multiple tspan elements from previous tip)
            while (tooltipelement.firstChild) {
                tooltipelement.removeChild(tooltipelement.firstChild);
            }
            // get the tooltip text from the data
            var str = d['label'];
            // split the tooltip by the new line delimiter
            var lines = str.split("/n");
            // compute the maximum number of lines we can display given the current height of the svg canvas
            var maxYLines = Math.min(Math.floor((svgH - (tooltip_padding * 2)) / line_height), lines.length);
            // if we cannot display all of the lines then the last line should be elipses
            if (maxYLines < lines.length)
            {
                lines[maxYLines - 1] = elipses;
            }
            // now add a tspan node to the tooltip DOM element
            var dy = 0; // initially delta y = 0 (line spacing adjustment)
            for (var i = 0; i < maxYLines; i++)
            {
                // create the tspan element
                var tspan = focustext.append("tspan");
                // set-up style and attributes
                tspan.attr("dy", dy);
                tspan.attr("x", 0);
                tspan.style("text-anchor", "middle");
                tspan.style("alignment-baseline", "central");
                tspan.text(lines[i]);
                // from this point onwards, delta y (line spacing) is line height
                dy = line_height;
                // do we need to truncate the text to fit the svg canvas?
                while (focustext.node().getBBox().width + tooltip_padding * 2 > svgW)
                {
                    // trim the text and add elipses
                    lines[i] = lines[i].substring(0, Math.max(0, lines[i].length - elipses.length - 1)) + elipses;
                    // update the tspan element with the trimmed text
                    tspan.text(lines[i]);
                }
            }
            // store the final width and height of the tooltip
            var w = focustext.node().getBBox().width + tooltip_padding * 2;
            var h = focustext.node().getBBox().height + tooltip_padding * 2;
            // store the desired x and y position of the tooltip (centre)
            var x = xAxisScale(d['x']);
            var y = yAxisScale(d['y']);


            // if the tooltip will go over the left border then clamp to left edge
            if (x - w / 2 < 0)
            {
                x = w / 2;
            }
            // if the tooltip will go over the right border then clamp to right edge
            else if (x + w / 2 > svgW)
            {
                x = svgW - w / 2;
            }
            // if the tooltip will go over the top border then clamp to top edge
            if (y - h / 2 < 0)
            {
                y = h / 2;
            }
            // if the tooltip will go over the bottom border then clamp to bottom edge
            else if (y + h / 2 > svgH)
            {
                y = svgH - h / 2;
            }
            // compute the displacement
            var displacementY = (dy * (maxYLines - 1)) / 2.0;
            // move the text

            focustext.attr("transform", "translate(" + x + "," + (y - displacementY) + ")");
            // move the box
            focus.attr("width", w);
            focus.attr("height", h);
            focus.attr("transform", "translate(" + (x - w / 2) + "," + (y - h / 2) + ")");

            var focusElement = document.getElementById("focus");
            focusElement.parentNode.appendChild(focusElement);
            tooltipelement.parentNode.appendChild(tooltipelement);
        }

        var tx = svgContainer.selectAll("#yaxistext").data([0]);
        tx.enter().append("text")
                .attr("class", "axistext")
                .attr("id", "yaxistext")
                .style("text-anchor", "middle")
                .style("writing-mode", "tb") // set the writing mode
                .style("font-size", fontSize)
                .style("font-family", "Arial,Helvetica")
                .attr("transform", "translate(" + 10 + "," + (h / 2) + ") rotate(180)");
        tx.text(this.data["axis"]["ylabel"]);

        var ty = svgContainer.selectAll("#xaxistext").data([0]);
        ty.enter().append("text")
                .attr("class", "axistext")
                .attr("id", "xaxistext")
                .style("text-anchor", "middle")
                .style("font-size", fontSize)
                .style("font-family", "Arial,Helvetica")
                .attr("transform", "translate(" + (w / 2) + " ," + (h - hPadding / 10) + ")");
        ty.text(this.data["axis"]["xlabel"]);
        //     // .text(function(d){return d["xlabel"];});

        var focus = svgContainer.selectAll(".focus").data([0]);
        focus.enter().append("rect")
                .attr("class", "focus")
                .attr("id", "focus")
                .style("stroke", "black")
                .style("fill", "white")
                .style("display", "none")
                .attr("width", "50px")
                .attr("height", "15px")
                .style("pointer-events", "none");

        var focustext = svgContainer.selectAll(".tooltiptext").data([0]);
        focustext.enter().append("text")
                .attr("id", "tooltip")
                .attr("class", "tooltiptext")
                .style("font-size", fontSize)
                .style("font-family", "Arial,Helvetica")
                .style("text-anchor", "middle")
                .style("alignment-baseline", "central")
                .style("pointer-events", "none");




        var legendboxElement = document.getElementById("legendbox");
        svgContainerElement.insertBefore(legendboxElement, svgContainerElement.firstChild);

    };
}

function getRange(data, element)
{
    if (data["points"].length > 0)
    {
        var min = data["points"][0][element];
        var max = min;
        for (var i = 1; i < data["points"].length; i++)
        {
            if (data["points"][i][element] < min)
            {
                min = data["points"][i][element];
            }
            if (data["points"][i][element] > max)
            {
                max = data["points"][i][element];
            }
        }

        return [0, max];
    }
    else
        return [0, 0];
}

function render_tplot(jsonFile, canvasID)
{
    the_tplot = new tplot(canvasID);
      d3.json(jsonFile, function (json) {
        the_tplot.data = json;
        the_tplot.visualise();
    });
}


