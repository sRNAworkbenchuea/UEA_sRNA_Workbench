// author chris


function hairpin(div, mature_s_index, mature_e_index, star_s_index, star_e_index) {

    this.div = div;
    this.mature_s_index = mature_s_index;
    this.mature_e_index = mature_e_index;
    this.star_s_index = star_s_index;
    this.star_e_index = star_e_index;
    this.secondary_canvasWidth = 0;
    this.secondary_canvasHeight = 0;
    this.secondary_canvasWidth = 10;
    this.line_width = 1;
    this.data = null;
    this.smallestLineLength = -1;

    this.setData = function (hairpinRenderData)
    {
        this.data = hairpinRenderData;
    };

    this.renderHairpin = function ()
    {
        if (this.data == null)
        {
            console.log("WARNING: No data input.");
            return;
        }
        var svgContainer = d3.select('#' + this.div);
        var con = document.getElementById(this.div);

        window.onresize = $.proxy(this.renderHairpin, this);

        this.secondary_canvasWidth = con.getBoundingClientRect().width;
        this.secondary_canvasHeight = con.getBoundingClientRect().height;
        this.secondary_padding = this.secondary_canvasWidth * 0.05;
      
        var secondary_hCanvasWidth = this.secondary_canvasWidth / 2;
        var secondary_hCanvasHeight = this.secondary_canvasHeight / 2;
        this.data = orientation(this.data, this.secondary_canvasHeight, this.secondary_canvasWidth);
        var scales = getScales(this.data, this.secondary_canvasWidth, this.secondary_canvasHeight, this.secondary_padding);
        var positions = getPositions(this.data, scales[0], scales[1]);
        var pairLines = [];
        for (i = 0; i < this.data.length; i++)
        {
            if (this.data[i].s != 0)
            {
                var sIndex = this.data[i].id - 1;
                var eIndex = this.data[i].e - 1;
                var x1 = positions[sIndex][0];
                var y1 = positions[sIndex][1];
                var x2 = positions[eIndex][0];
                var y2 = positions[eIndex][1];
                pairLines.push([x1, x2, y1, y2]);
            }
        }
        var perimeterLines = [];
        this.smallestLineLength = -1;
        for (i = 0; i < positions.length - 1; i++)
        {
            perimeterLines.push([positions[i][0], positions[i + 1][0], positions[i][1], positions[i + 1][1]]);
            var lineLength = Math.sqrt((positions[i][0] - positions[i + 1][0]) * (positions[i][0] - positions[i + 1][0]) + (positions[i][1] - positions[i + 1][1]) * (positions[i][1] - positions[i + 1][1]));
            if (this.smallestLineLength == -1 || lineLength < this.smallestLineLength)
            {
                this.smallestLineLength = lineLength;

            }
        }

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


        var lines = svgContainer.selectAll(".pairLines")
                .data(pairLines);
        // update
        lines
                .transition()
                .attr("x1", function (d) {
                    return d[0];
                })
                .attr("x2", function (d) {
                    return d[1];
                })
                .attr("y1", function (d) {
                    return d[2];
                })
                .attr("y2", function (d) {
                    return d[3];
                });
        lines.enter().append("line")
                .attr("stroke", "gray")
                .attr("stroke-width", this.line_width)
                .attr("x1", secondary_hCanvasWidth)
                .attr("x2", secondary_hCanvasWidth)
                .attr("y1", secondary_hCanvasHeight)
                .attr("y2", secondary_hCanvasHeight)
                .attr("class", "pairLines")
                .transition()
                .attr("x1", function (d) {
                    return d[0];
                })
                .attr("x2", function (d) {
                    return d[1];
                })
                .attr("y1", function (d) {
                    return d[2];
                })
                .attr("y2", function (d) {
                    return d[3];
                });
        lines.exit().remove();

        // data join
        var outsideLines = svgContainer.selectAll(".perimeter")
                .data(perimeterLines);
        // update
        outsideLines
                .transition()
                .attr("x1", function (d) {
                    return d[0];
                })
                .attr("x2", function (d) {
                    return d[1];
                })
                .attr("y1", function (d) {
                    return d[2];
                })
                .attr("y2", function (d) {
                    return d[3];
                });
        // enter
        outsideLines.enter().append("line")
                .attr("stroke", function (d, i)
                {
                    if (i >= mature_s_index && i < mature_e_index)
                        return "LightCoral";
                    else if (i >= star_s_index && i < star_e_index)
                        return "LightBlue";
                    return "gray";
                })
                .attr("stroke-width", this.line_width)
                .attr("x1", secondary_hCanvasWidth)
                .attr("x2", secondary_hCanvasWidth)
                .attr("y1", secondary_hCanvasHeight)
                .attr("y2", secondary_hCanvasHeight)
                .attr("class", "perimeter")
                .transition()
                .attr("x1", function (d) {
                    return d[0];
                })
                .attr("x2", function (d) {
                    return d[1];
                })
                .attr("y1", function (d) {
                    return d[2];
                })
                .attr("y2", function (d) {
                    return d[3];
                });
        // exit
        outsideLines.exit().remove();

        var circles = svgContainer.selectAll(".nuc_circle")
                .data(positions);
        circles.on("mousemove", showNucleotide);
        // update
        circles
                .transition()
                .attr("r", this.smallestLineLength / 3.0)

                .attr("cx", function (d) {
                    return d[0];
                })
                .attr("cy", function (d) {
                    return d[1];
                });
        circles.enter().append("circle")
                .style("fill", function (d, i) {
                    if (i >= mature_s_index && i <= mature_e_index)
                        return "LightCoral";
                    else if (i >= star_s_index && i <= star_e_index)
                        return "LightBlue";
                    return "white";

                })
                .style("stroke", "black")
                .attr("r", this.smallestLineLength / 3.0)
                .attr("cx", secondary_hCanvasWidth)
                .attr("cy", secondary_hCanvasHeight)
                .attr("class", "nuc_circle")
                .transition()
                .attr("cx", function (d) {
                    return d[0];
                })
                .attr("cy", function (d) {
                    return d[1];
                });
        circles.exit().remove();

        var text = svgContainer.selectAll("text")
                .data(positions);
        text
                .text(function (d) {
                    return d[2];
                })
                .attr("font-size", (this.smallestLineLength / 3.0) + "px")
                .transition()
                .attr("x", function (d) {
                    return d[0];
                })
                .attr("y", function (d) {
                    return d[1];
                });
        text.enter().append("text")
                .style("fill", "black")
                .attr("x", secondary_hCanvasWidth)
                .attr("font-family", "sans-serif")
                .attr("y", secondary_hCanvasHeight)
                .attr("font-size", (this.smallestLineLength / 3.0) + "px")
                .attr("text-anchor", "middle")
                .attr("dy", ".35em")
                .text(function (d) {
                    return d[2];
                })
                .transition()
                .attr("x", function (d) {
                    return d[0];
                })
                .attr("y", function (d) {
                    return d[1];
                });
        text.exit().remove();




    };
}
function map(raw_x1, raw_y1, raw_x2, raw_y2, tgt_x1, tgt_y1, tgt_x2, tgt_y2)
{
    // get the raw dimensions
    var raw_dim_x = raw_x2 - raw_x1;
    var raw_dim_y = raw_y2 - raw_y1;
    var raw_mid_x = (raw_x1 + raw_x2) / 2;
    var raw_mid_y = (raw_y1 + raw_y2) / 2;
    var raw_aspect = raw_dim_x / raw_dim_y;

    // get the target aspect ratio
    var tgt_dim_x = tgt_x2 - tgt_x1;
    var tgt_dim_y = tgt_y2 - tgt_y1;
    var tgt_aspect = tgt_dim_x / tgt_dim_y;


    var ratio = raw_aspect / tgt_aspect;
    var domain_x1 = raw_x1;
    var domain_x2 = raw_x2;
    var domain_y1 = raw_y1;
    var domain_y2 = raw_y2;
    if (ratio > 1)
    {
        domain_y1 = raw_mid_y - raw_dim_y * ratio / 2;
        domain_y2 = raw_mid_y + raw_dim_y * ratio / 2;
    }
    else
    {
        domain_x1 = raw_mid_x - raw_dim_x / ratio / 2;
        domain_x2 = raw_mid_x + raw_dim_x / ratio / 2;
    }
    var xScale = d3.scale.linear()
            .domain([domain_x1, domain_x2])
            .range([tgt_x1, tgt_x2]);
    var yScale = d3.scale.linear()
            .domain([domain_y1, domain_y2])
            .range([tgt_y1, tgt_y2]);
    return [xScale, yScale];

}

function getScales(data, secondary_canvasWidth, secondary_canvasHeight, secondary_padding) {
    var minX = min(data, "x");
    var maxX = max(data, "x");
    var minY = min(data, "y");
    var maxY = max(data, "y");
    return map(minX, minY, maxX, maxY, secondary_padding, secondary_padding, secondary_canvasWidth - secondary_padding, secondary_canvasHeight - secondary_padding);
}
function getPositions(data, scaleX, scaleY) {
    var positions = [];
    positions.length = data.length;
    for (i = 0; i < data.length; i++) {
        positions[data[i].id - 1] = ([scaleX(data[i].x), scaleY(data[i].y), data[i].n]);
    }
    return positions;
}
function min(data, key) {
    if (data.length > 0) {
        var minValue = parseFloat(data[0][key]);
        for (i = 0; i < data.length; i++) {
            var v = parseFloat(data[i][key]);
            if (v < minValue) {
                minValue = v;
            }
        }
        return minValue;
    }
    console.log("WARNING: No data values.");
    return null;
}
function max(data, key) {
    if (data.length > 0) {
        var maxValue = parseFloat(data[0][key]);
        for (i = 0; i < data.length; i++) {
            var v = parseFloat(data[i][key]);
            if (v > maxValue) {
                maxValue = v;
            }
        }
        return maxValue;
    }
    console.log("WARNING: No data values.");
    return null;
}
function orientation(data, secondary_canvasHeight, secondary_canvasWidth) {
    var xDiff = max(data, "x") - min(data, "x");
    var yDiff = max(data, "y") - min(data, "y");
    if ((xDiff > yDiff && secondary_canvasHeight > secondary_canvasWidth) || (xDiff < yDiff && secondary_canvasHeight < secondary_canvasWidth)) {
        for (var i = 0; i < data.length; i++) {
            var tmp = data[i].x;
            data[i].x = -data[i].y;
            data[i].y = tmp;
        }
    }
    return data;
}


var hp = null;
function renderHairpin(xrna_file, div, mature_s_index, mature_e_index, star_s_index, star_e_index)
{

    hp = new hairpin(div, mature_s_index, mature_e_index, star_s_index, star_e_index);
    var dsv = d3.dsv(" ", "text/plain");
    dsv(xrna_file, callback_function);

}

function clearHairpin(divID)
{
    $("#" + divID).empty();
}
function callback_function(data)
{
    hp.setData(data);
    hp.renderHairpin();
}


function showNucleotide(d)
    {
//        console.log(d[[2]]);
  //var x = xAxisScale(d['x']);
    //    var y = yAxisScale(d['y']);
        focus.attr("transform", "translate(" + d[0] + "," + d[1] + ")");
    }
