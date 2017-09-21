// This holds the singular FileTable object, which is then referenced within the object itself.
var filetable;

/**
 * Create a new FileTable.
 * @param {type} divID the dive element that will contain the FileTable. The 
 *  table is appended to rest of the content within this div
 * @param {type} setupMode true if the table is in setup mode (button to add new 
 *  replicates / samples) or review mode (checkboxes for deselecting replicates)
 * @returns {FileTable} the FileTable object if called using new FileTable()
 */
var newFileTable = function(divID, setupMode)
{
    filetable = new FileTable(divID, setupMode, "filetable");
};

/**
 * Called by the above method. Use the above method to ensure a global variable that
 * is correctly referenced inside the object
 */
function FileTable(target, options)
{
    this.target = target;
    this.options = options;// this.ref = filetable_ref;
    this.img_cross = "<img src='../../images/cross_black.png' alt='Cross' style='width:12px;height:12px'>";
    this.totalNumberOfSamples = 0;
     //this.table = thisdiv.getElementsByTagName("table")[0];
     
     // is this table in setupMode?
     //this.setupMode = setupMode;
     
     var getFileCellHTML = function(sampleID, rowCount, callback, filename)
     {
         if(typeof filename === "undefined")
             filename = "Add File";
         if(this.setupMode || typeof callback !== "string") // in setup mode, make filename clickable
            return "<a href='#' onclick=\""+callback+"()>"+filename+"</a>";
        return filename; // in non-setup mode - no linky
     };
     
     this.tableIDprefix = '_replicateInputTable_';
     
     
     var _init = function($this){
         //var thisdiv = document.getElementById(divID);
         var tble = $("<table></table>")
                 .attr("class", "filetable")
                 .attr("id", "fileInputTable")
                 .attr("cellspaing", "0");
         
         
         var header = $("<thead/>").append($("<tr/>")
                 .append($("<th/>").text("Sample ID").attr("id", "sampleID_Header"))
                 .append($("<th/>").text("Small RNA Replicate Details").attr("id", "sRNA_Header"))
                 .append($("<th/>").text("Degradome Replicate Details").attr("id", "deg_Header")));
         
         tble.append(header).append($("<tbody/>").addClass("ftb"));
         $this.target.append(tble);

        //                                        <!--<th id="mRNA_Header">
        //                                            mRNA Replicate Details 
        //                                        </th>-->
        };
    
    var _getNewReplicateRow = function($this, sampleID, replicateID, file, dataType)
    {
        var id = sampleID + "_replicateRow_" + replicateID;
        var newRow = $("<tr/>").addClass("replicateRow");
        var label = $("<a href='#'\>").text(replicateID);
        var labelCell = $("<td/>").append(label).addClass("label");
        
        if ($this.options.setup_mode) {
            // add ability to remove sample if table is setupable
            label.append($($this.img_cross))
                    .click((function () {
                        $this.removeReplicate(sampleID, replicateID, dataType)
                    }).bind($this));
        }
        
        var checkbox = $("<input/>")
                .attr("type", "checkbox")
                .attr("name", "replicateChecks")
                .attr("id", sampleID + "_replicateEnabled_"+replicateID)
                .click((function(){
                    this.options.on_include_state_change(sampleID, replicateID)
                }).bind($this))
                .prop("checked", true);
        var includeCell = $("<td/>").append(checkbox);
        var filenameCell = $("<td/>").addClass("filename")
                .append($("<a href='#'/>").text(file)
                .click((function () {
                    $this.set_file(sampleID, replicateID, dataType)
                })));
        newRow.append(labelCell).append(filenameCell);
        if(!$this.options.setup_mode)
            newRow.append(includeCell);
        
        // degradome specific cells
        if(dataType === "deg")
        {
            var obj = $this;
            newRow.append($("<td>").append($("<select id='" + id+"_dataType'>" +
                    "<option value = '0' >Degradome</option>" +
                    "<option value = '1' >PAREsnip Output</option>" +
                    "</select>")
                    .change((function()
            {
                obj.setDegDataType(sampleID, replicateID, $(this).children("option:selected").val());
            }))));
        }
        return newRow;
    }

    FileTable.prototype.addSample = function(sampleID){
        var tbody = this.target.find(".ftb");
        var rowCount = this.target.find(".ftb").children("tr.sampleRow").length;
        if(typeof sampleID === "undefined" || sampleID === "")
        {
            sampleID = ++this.totalNumberOfSamples;
        }
        var row = $("<tr/>").attr("id",this.tableIDprefix + sampleID).addClass("sampleRow");
        var srnaCell = $("<td/>");
        var degCell = $("<td/>");
        
        var label = $("<a href='#'\>").text(sampleID);
        
        if(this.options.setup_mode){
            // add ability to remove sample if table is setupable
                label.append($(this.img_cross))
                .click((function(){this.removeSample(sampleID)}).bind(this));
        }
        var labelCell = $("<td/>").append(label);
                
        row.append(labelCell).append(srnaCell).append(degCell);
        tbody.append(row);

        var srnatableID = "sRNA_replicateInputTable_" + sampleID;
        var srna_table = $("<table/>").addClass("filetable").attr("id", srnatableID).attr("cellspacing", "0");
        
        var degtableID = "deg_replicateInputTable_" + sampleID;
        var deg_table = $("<table/>").addClass("filetable").attr("id", degtableID).attr("cellspacing", "0");
        
        // header
        var header = $("<thead/>")
        var hrow = $("<tr/>")
                .append($("<th/>").text("Replicate Number"))
                .append($("<th/>").text("Filename"));
        
        // add include column if in review mode
        if(!this.options.setup_mode)
                hrow.append($("<th/>").text("Include?"));           
        header.append(hrow); 
        var deg_header = header.clone();
        deg_header.children("tr").append("<th>Type</type>");
        
        srna_table.append(header).append("<tbody/>");     
        srnaCell.append(srna_table);
        
        deg_table.append(deg_header).append("<tbody/>");
        degCell.append(deg_table);
        
        // Add File clickable row for setupable tables
        var rowId = sampleID + "_replicateRow_" + rowCount;
        var newRow =$("<tr/>").addClass("addFileRow"); // insert AFTER the header, which is 0th position
        newRow.attr("id", rowId);
        var srna_cell = $("<td\>")
                .addClass("addFile")
                .attr("colSpan", 3)
                .append($("<a\>").attr("href", '#').text("Add Files")
                .click((function(){
                    this.add_file(sampleID, "sRNA");
                }).bind(this)));
                
        var deg_cell = srna_cell.clone()
        deg_cell.children("a")
                .click((function(){
                    this.add_file(sampleID, "deg");
                }).bind(this));
        
        var newDegRow = newRow.clone();
        srna_table.children("tbody").append(newRow.append(srna_cell));
        deg_table.children("tbody").append(newDegRow.append(deg_cell));

        if(!this.options.setup_mode){
            // hide the Add Files row if in review mode
            newRow.hide();
        }
        return sampleID;
        
    };
    
    /**
     * 
     * @param {type} sampleID ID of the sample to add to
     * @param {type} files array of files (string)
     * @param {type} dataType can be "sRNA" to add to the sRNA table or "deg" to add to the degradome table
     * @returns {undefined}
     */
    FileTable.prototype.add_file = function(sampleID, dataType, files){
        
        var replicateTable = $('#'+dataType+this.tableIDprefix+sampleID).find("tbody");
        var rows = replicateTable.find("tr");
        var rowCount = rows.length;
        var replicateID = rowCount;
        
        if(typeof files === "undefined")
            if(dataType==="sRNA")
                files = this.options.on_add_sRNA_file(sampleID,replicateID);
            if(dataType==="deg")
                files = this.options.on_add_deg_file(sampleID,replicateID);
        
        if(typeof files !=="undefined"){
            console.log(files);
            var addFileRow = replicateTable.find(".addFileRow");
            addFileRow.before(_getNewReplicateRow(this, sampleID, replicateID, files[0], dataType));
            replicateID++;

            for(var i = 1; i < files.length; i++)
            {
                addFileRow.before(_getNewReplicateRow(this, sampleID, replicateID, files[i], dataType))
                replicateID++;
            }
        }
    };
    
    FileTable.prototype.setDegDataType = function(sampleID, replicateID, type)
    {
        this.options.on_change_deg_type(sampleID, replicateID-1, type);
    }
    
    
    FileTable.prototype.set_file = function(sampleID, replicateID, type, file){
        var rows = $('#'+type+this.tableIDprefix+sampleID).find("tbody tr");
        var thisRow = rows.eq(replicateID-1);
        
        if(typeof file === "undefined"){
            if(type === "sRNA"){
                file = options.on_set_sRNA_file(sampleID, replicateID);
            } else{
                file = options.on_set_deg_file(sampleID, replicateID);
            }
        }
        
        if(typeof file !== "undefined")
        {
            thisRow.find(".filename a").text(file);
        }
        
    };
    
    FileTable.prototype.removeSample = function(sampleID)
    {
        $('#'+this.tableIDprefix+sampleID).remove();
        if(typeof this.options.on_remove_sample === "function")
            this.options.on_remove_sample(sampleID);

    };
    FileTable.prototype.removeReplicate = function (sampleID, replicateID, type)
    {
        if(type === "sRNA"){
            if (typeof this.options.on_remove_sRNA_replicate === "function")
                this.options.on_remove_sRNA_replicate(sampleID, replicateID-1);
        }
        else
        {
            if (typeof this.options.on_remove_deg_replicate === "function")
                this.options.on_remove_deg_replicate(sampleID, replicateID-1);
        }
        
        var repRows = $('#' + type + this.tableIDprefix + sampleID).find("tbody tr.replicateRow")
        repRows.eq(replicateID-1).remove();
        repRows = $('#' + type + this.tableIDprefix + sampleID).find("tbody tr.replicateRow")
        repRows.each((function(i,r){
            
            var labelCell = $(r).children("td.label")
            labelCell.empty();
            var label = $("<a href='#'\>");
            labelCell.append(label);
            label.text(i+1)
            label.append($(this.img_cross))
                    .click((function () {
                        this.removeReplicate(sampleID, i+1, type)
                    }).bind(this));
        }).bind(this))
    };
    
    FileTable.prototype.clearTable = function()
    {
        this.target.find(".ftb").empty();
    }
    
    _init(this);
};

(function($){
    $.fn.fileTable = function(method) {
        var args = arguments;
        var ftObj = $(this).data('fileTable'); // retrieve any previously stored FileTable object
        if (typeof method === 'object' || ! method ) {
            // method is options for new FileTable
            var options = $.extend({}, $.fn.fileTable.defaults, method || {});
            var filetable = new FileTable($(this), options);
            $(this).data('fileTable', filetable);
        }
        else if (typeof FileTable.prototype[method] === "function") {
            // method is a function to call
            // return result of calling the function with the extra arguments
            // calls the method using the retrieved ftObj
            return FileTable.prototype[method].apply(ftObj, Array.prototype.slice.call(args, 1));
        }
    };
    /*
     * Fill in these options to register functions that can be called outside of
     * FileTable to occur when needed. Many of these callbacks DO pass parameters through
     */
    $.fn.fileTable.defaults = {
        on_add_sRNA_file: null,
        on_add_deg_file: null,
        on_set_sRNA_file: null,
        on_set_deg_file: null,
        on_add_sample: null,
        on_change_deg_type: null,
        setup_mode: true
        
    };
    
})(jQuery);


