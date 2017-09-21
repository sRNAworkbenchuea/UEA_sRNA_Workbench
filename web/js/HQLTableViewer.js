function createDataTable(tableHeaderContent, containerID, initialisationStr)
{
    console.log("dt version: " + $.fn.dataTable.version);
    var container = document.getElementById(containerID);
    if (container != null)
    {

        while (container.hasChildNodes()) {
            container.removeChild(container.lastChild);
        }

        var tableElement = document.createElement("table");
        tableElement.setAttribute("id", containerID + "_table");
        //tableElement.setAttribute("class", "stripe");
        tableElement.setAttribute("class", "dt-right");
        //tableElement.setAttribute("style", "width:100%;");

        var thead = document.createElement("thead");
        var tbody = document.createElement("tbody");
        if (tableHeaderContent == "")
        {
            tableHeaderContent = "<tr><th></th></tr>";
        }
        thead.innerHTML = tableHeaderContent;
//        var tr = document.createElement("tr");
        //tbody.innerHTML =  "<tr><th rowspan='2'>sRNA</th><th rowspan='2'>Annotation</th><th colspan='2'>sRNA File 1</th><th colspan='2'>sRNA File 2</th></tr><tr><th>Raw Abundance</th><th>Cat</th><th>Raw Abundance</th><th>Cat</th></tr>";

        //for (i = 0; i < columnHeaderArray.length; i++)
        // {
        //     var th = document.createElement("th");
        //    th.textContent = columnHeaderArray[i];
        //    tr.appendChild(th);
        // }
        // thead.appendChild(tr);
        tableElement.appendChild(thead);
        tableElement.appendChild(tbody);
        container.appendChild(tableElement);

        var tableID = "#" + containerID + "_table";

        var table = $(tableID).DataTable(initialisationStr);
        table.on('order.dt search.dt', function () {
            table.column(0, {search: 'applied', order: 'applied'}).nodes().each(function (cell, i) {
                cell.innerHTML = i + 1;
            });
        }).draw();

    }
}


function setSelectableDataTable(containerID, onSelectFunc)
{
    var tableID = "#" + containerID + "_table";
    var table = $(tableID).DataTable();
    $(tableID + ' tbody').on('click', 'tr', function () {
        if ($(this).hasClass('selected')) {
            $(this).removeClass('selected');
        }
        else {
            table.$('tr.selected').removeClass('selected');
            $(this).addClass('selected');
            var str = "";
            for (var i = 0; i < table.row(this).data().length; i++)
            {
                str += table.row(this).data()[i] + "<FIELD_SEP>";
            }
            onSelectFunc(str);
        }
    });
    $('#button').click(function () {
        table.row('.selected').remove().draw(false);
    });
}

function getDataTablesDataAsCSV(tableContainerID)
{
    var tableID = "#" + tableContainerID + "_table";
    var table = $(tableID).DataTable();
    var data = table.rows().data();
    var headerrow = table.columns().header();
    var str = "";
    for (i = 0; i < headerrow.length; i++)
    {
        if (i > 0)
        {
            str += ",";
        }
        str += headerrow[i].innerText;
    }
    for (i = 0; i < data.length; i++)
    {
        str += "\n";
        str += data[i];
    }
    return str;
}

function saveTableData(table, data)
{
    try
    {
        var headerrow = table.columns().header();
        var str = "";
        for (i = 0; i < headerrow.length; i++)
        {
            if (i > 0)
            {
                str += ",";
            }
            str += headerrow[i].innerText;
        }
        for (i = 0; i < data.length; i++)
        {
            str += "\n";
            str += data[i];
        }
        app.saveDialogCSV(str, false);
    }
    catch (err)
    {
        throw "Error saving table data: " + err;
    }
}

function saveTable(containerID)
{
    try
    {
        var tableID = "#" + containerID + "_table";
        var table = $(tableID).DataTable();
        var data = table.rows().data();
        saveTableData(table, data);
    }
    catch (err)
    {
        throw "Error saving table data: " + err;
    }

}

function saveTableView(containerID)
{
    var tableID = "#" + containerID + "_table";
    var table = $(tableID).DataTable();
    var data = table.rows({search: 'applied'}).data();
    saveTableData(table, data);
}

function addToTable(str, containerID)
{
    var tableID = "#" + containerID + "_table";
    var table = $(tableID).DataTable();
    if (table != null)
    {
        table.row.add(str);
    }
    else
    {
        console.log("cannot find: " + tableID);
    }

}

function drawTable(containerID)
{
    var tableID = "#" + containerID + "_table";
    var table = $(tableID).DataTable();
    if (table != null)
    {
        table.draw();
    }
    else
    {
        console.log("cannot find: " + tableID);
    }

}
