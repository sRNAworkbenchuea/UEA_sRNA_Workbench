/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function loadHierarchy()
{

    var margin = {top: 0, right: 320, bottom: 0, left: 0},
    width = 960 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

    var tree = d3.layout.tree()
            .separation(function (a, b) {
                return a.parent === b.parent ? 1 : .5;
            })
            .children(function (d) {
                return d.parents;
            })
            .size([height, width]);

    //console.log(document.getElementById()("hierarchy_tree"));
    d3.select(".hierarchy_tree").remove();
    //console.log(document.getElementsByClassName("hierarchy_tree"));

    var svg = d3.select("#tree-container").append("svg")
            .attr("class", "hierarchy_tree")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

//console.log(document.getElementsByClassName("hierarchy_tree"));
    d3.json("../json/FileHierarchySQData.json", function (json) {
        //alert("loading data");
        //alert(json.name);
        if (json)
        {
            var nodes = tree.nodes(json);


            var link = svg.selectAll(".link")
                    .data(tree.links(nodes))
                    .enter().append("path")
                    .attr("class", "link")
                    .attr("d", elbow);

            var node = svg.selectAll(".node")
                    .data(nodes)
                    .enter().append("g")
                    .attr("class", "node")
                    .attr("id", function (d) {
                        //console.log(d.id);
                        link.attr("id", d.id);
                        return d.id;
                    })
                    .attr("transform", function (d) {
                        return "translate(" + d.y + "," + d.x + ")";
                    });

            node.append("text")
                    .attr("class", "name")
                    .attr("x", 8)
                    .attr("y", -6)
                    .text(function (d) {
                        return d.name;
                    });

            node.append("text")
                    .attr("x", 8)
                    .attr("y", 8)
                    .attr("dy", ".71em")
                    .attr("class", "about lifespan")
                    .text(function (d) {
                        //console.log(d.type);
                        if (d.type !== 'undefined')
                            return d.type;
                        else
                            return '';
                    });

            node.append("text")
                    .attr("x", 8)
                    .attr("y", 8)
                    .attr("dy", "1.86em")
                    .attr("class", "about location")
                    .text(function (d) {
                        //console.log(d);
                        if (d.extra !== 'undefined')
                            return d.location;
                        else
                            return '';
                    });
        }
    });

    function elbow(d, i) {
        return "M" + d.source.y + "," + d.source.x
                + "H" + d.target.y + "V" + d.target.x
                + (d.target.children ? "" : "h" + margin.right);
    }
}