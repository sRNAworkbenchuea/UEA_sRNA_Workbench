// author: chris
function ToolParameters(name, containerID) {

    this.params = [];
    this.maximised = true;

    this.addParameter = function (id, name, type, val)
    {
        try
        {
            this.params.push({id: id, name: name, type: type, val: val});
        }
        catch (err)
        {
            alert("There was an error adding '" + id + ", " + name + " to the parameter set.");
        }
    };



    this.disableInputs = function ()
    {
        try
        {
            for (i = 0; i < this.params.length; i++)
            {
                document.getElementById(this.params[i].id).disabled = true;
            }
        }
        catch (err)
        {
            alert("There was an error disabling inputs.");
        }
    };

    this.toggleSize = function ()
    {
        try
        {
            if (this.maximised)
            {
                document.getElementById(containerID).style.width = "10px";
                this.maximised = false;
            }
            else
            {
                document.getElementById(containerID).style.width = "auto";
                this.maximised = true;
            }
        }
        catch (err)
        {
            alert("There was an error toggling the size of the parameter box.");
        }
    };

    this.displayParameters = function ()
    {

        try
        {
            var html = "<h2 onclick='parameterSet.toggleSize();'>Parameters</h2><table>";
            for (i = 0; i < this.params.length; i++)
            {
                var className = "parameter";
                if (i % 2 == 1)
                    className += " evenrow";
                html += "<tr class=\"" + className + "\" id=\"parameter_" + this.params[i].id + "\">";
                html += "<td class=\"parameter_name\">" + this.params[i].name + "</td>";
                html += "<td class=\"parameter_value\">";
                if (this.params[i].type == "checkbox")
                {
                    //    var checkedStr = "";
                    //   if (this.params[i].val)
                    //  {
                    //     checkedStr = "checked";
                    // }
                    // else
                    // {
                    //     checkedStr = "unchecked";
                    //}
                    html += "<input type=\"checkbox\" class=\"css-checkbox\" id=\"" + this.params[i].id + "\" onfocusout=\"" + name + ".saveParam(this, '" + this.params[i].type + "');\" value=\"\"/>";
                    html += "<label for=\"" + this.params[i].id + "\" class=\"css-label\"></label>";
                }
                else
                {
                    html += "<input type=\"text\" id=\"" + this.params[i].id + "\" onfocusout=\"" + name + ".saveParam(this, '" + this.params[i].type + "');\" value=\"\"/>";

                }
                html += "</td>";
                html += "</tr>";
            }
            html += "</table>";
            document.getElementById(containerID).innerHTML = html;
            //save();
            app.displayParameters();
        }
        catch (err)
        {
            alert("There was an error displaying the parameter set.");
        }
    };

    this.saveParam = function (element, type)
    {
        try
        {
            var valid = true;
            switch (type) {
                case "int":
                    {
                        // cast to int to check data compatibility
                        var intValue = parseInt(element.value);
                        if (intValue != element.value)
                        {
                            valid = false;
                        }
                        else
                        {
                            // removes any formatting issues (leading zeros)
                            element.value = intValue;
                        }
                    }
                    break;
                case "float":
                case "double":
                {
                    var floatVal = parseFloat(element.value);
                    if (floatVal != element.value)
                    {
                        valid = false;
                    }
                    else
                    {
                        // removes any formatting issues (leading zeros)
                        element.value = floatVal;
                    }
                    break;
                }
            }

            if (valid)
            {
                valid = save();
            }
            var containerElement = document.getElementById("parameter_" + element.id);
            if (!valid) {
                element.value = "";
                element.focus();
                containerElement.className = "parameter_invalid";
            }
            else
            {
                containerElement.className = "parameter";

            }
        }
        catch (err)
        {
            alert("There was an error saving a parameter.");
        }
    };

}