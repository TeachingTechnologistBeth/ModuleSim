/*
 * scripts for Hex8 emulator/debugger
 */

function assert(a) {
    if (!a) {
        throw "Assertion failed";
    }
}

function is_byte(b) {
    if (b == undefined) { return false; }
    if (b != parseInt(b, 10)) { return false; }
    return (b >= 0 && b <= 255);
}

var HEX = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'];

function hex_byte(i) {
    assert(is_byte(i));
    return HEX[i >> 4] + HEX[i % 16];
    
}

/* data mdoel */

var model = {
    memory : [],
    registers: { a: 0, b: 0, p: 0, o: 0 },
    running: 0
};

function init_model(m) {
    m.running = 0;
    m.registers.a = 0;
    m.registers.b = 0;
    m.registers.p = 0;
    m.registers.o = 0;
    for (var i = 0; i < 256; i++) {
        m.memory[i] = 0;
    }
}

/* model observers */

var reg_observers = [];
var mem_observers = [];

// read or write a register. Write fires off a changed event.
function reg(n, v) { 

    if (v == undefined) {
        // read
        return model.registers[n];
    } else {
        // write
        assert(v >= 0 && v <= 255);
        model.registers[n] = v;
        for (var i = 0; i < reg_observers.length; i++) {
            (reg_observers[i])(n, v);
        }
    }
}

function to_byte(v) {
    if (v == undefined) { return undefined; }
    if (v < 0) { v += 256; }
    return v % 256;
}

function ra(v) { return reg("a", to_byte(v)); }
function rb(v) { return reg("b", to_byte(v)); }
function rp(v) { return reg("p", to_byte(v)); }
function ro(v) { return reg("o", to_byte(v)); }

// read or write memory. Triggers observer events.
function mem(addr, v) {
    assert(is_byte(addr));
    if (v == undefined) {
        var w = model.memory[addr];
        for (var i = 0; i < mem_observers.length; i++) {
            (mem_observers[i])("r", addr, w);
        }
        return w;
    } else {
        assert(is_byte(v));
        var old = model.memory[addr];
        model.memory[addr] = v;
        for (var i = 0; i < mem_observers.length; i++) {
            (mem_observers[i])("w", addr, old, v);
        }
        return v;
    }
}

// read memory without triggering events.
function mem2(addr) {
    assert(is_byte(addr));
    return model.memory[addr];
}

// Update entire memory from buffer.
function mem_update(buf) {
    assert(buf.length <= 256);
    for (var index = 0; index < buf.length; index++) {
        model.memory[index] = buf[index];
    }
    for (var index = buf.length; index < 256; index++) {
        model.memory[index] = 0;
    }
    for (var index = 0; index < mem_observers.length; index++) {
        (mem_observers[index])("reload");
    }
}

/* views */

// Update the control panel. Listens for register changes.
function update_control(n, v) {
    if (n == "run") {
        if (v) {
            // $("#reset").attr('disabled', true);
            $("#step").attr('disabled', false);
            $("#run").attr('disabled', false);
            $("#is_running").html("Running.");
        } else {
            // $("#reset").attr('disabled', false);
            $("#step").attr('disabled', true);
            $("#run").attr('disabled', true);
            $("#is_running").html("Not running.");
        }
    }
}

// messages
function clearError() {
    $("#messages_content").html("");
    $("#messages").hide();
}

function reportError(e) {
    $("#messages_content").html(e);
    $("#messages").show();
}

// Called when the simulator has read or written memory or memory has reloaded.
// rw = "r" or "w" or "reload", addr is the memory address, for a read
// a is the value read, for a write a, b are the old and new values.
function update_mem_view(rw, addr, a, b) {
    $(".mem_cell").removeClass("r1");
    $(".mem_cell").removeClass("w1");

    var cellname = "#mem_" + (addr >> 4) + "_" + (addr % 16);
    if (rw == "r") {
        $(cellname).addClass("r1");
    } else if (rw == "w") {
        $(cellname).html(hex_byte(mem2(addr)));
        $(cellname).addClass("w1");
    } else if (rw == "reload") {
        for (var i = 0; i < 16; i++) {
            for (var j = 0; j < 16; j++) {
                var cell = $("#mem_" + i + "_" + j);
                cell.html(hex_byte(mem2(16*i+j)));
            }
        }
    }
}

// Set up the disassembler view - create components and register the observer.
function init_disassembly_view() {
    var view = $("#disassembly");
    view.html("");

    var content = document.createElement("table");
    content.className = "dis_table";

    for (var i = 0; i < 256; i++) {
        var row = document.createElement("tr");
        var heading = document.createElement("td");
        heading.className = "row_header";
        heading.innerHTML = hex_byte(i);
        row.appendChild(heading);
        var cell = document.createElement("td");
        cell.id = "dis_h_" + i;
        cell.className = "mem_cell";
        cell.innerHTML = hex_byte(mem2(i));
        row.appendChild(cell);
        var disasm = document.createElement("td");
        disasm.id = "dis_d_" + i;
        disasm.className = "dis_cell";
        disasm.innerHTML = disassemble(mem2(i));
        row.appendChild(disasm);
        var breakpoint = document.createElement("td");
        breakpoint.innerHTML = "<input type=\"checkbox\" id=\"dis_b_" + i +
            "\" />";
        row.appendChild(breakpoint);
        content.appendChild(row);
    }

    view.append(content);
    mem_observers.push(update_disassembly_view); 
}

// Set up the memory view and register the observer.
function init_memory_view() {
    var view = $("#memory");
    view.html("");

    var content = document.createElement("table");
    content.className = "mem_table";
    var top_row = document.createElement("tr");
    top_row.appendChild(document.createElement("td"));
    for (var col = 0; col < 16; col++) {
        var h = document.createElement("td");
        h.className = "col_header";
        h.innerHTML = hex_byte(col);
        top_row.appendChild(h);
    }
    content.appendChild(top_row);
    for (var row = 0; row < 16; row++) {
        var theRow = document.createElement("tr");
        var row_header = document.createElement("td");
        row_header.className = "row_header";
        row_header.innerHTML = hex_byte(row << 4);
        theRow.appendChild(row_header);
        for (var col = 0; col < 16; col++) {
            var theCell = document.createElement("td");
            theCell.id = "mem_" + row + "_" + col;
            theCell.className = "mem_cell";
            theCell.innerHTML = hex_byte(mem2(16*row + col));
            theRow.appendChild(theCell);
        }
        content.appendChild(theRow); 
    }
    view.append(content);

    mem_observers.push(update_mem_view);
}

// update the register view. n=name, v=value
function update_registers(n, v) {
    if(n == "a")       { $("#areg").html(hex_byte(v)); }
    else if (n == "b") { $("#breg").html(hex_byte(v)); }
    else if (n == "p") { $("#preg").html(hex_byte(v)); }
    else if (n == "o") { $("#oreg").html(hex_byte(v)); }
}

// update disassembly when a memory cell changes
function update_disassembly_view(rw, addr, a, b) {
    if (rw == "r") {
        // nothing to do
    } else if (rw == "w") {
        // update cell
        $("#dis_h_" + addr).html(hex_byte(b));
        $("#dis_d_" + addr).html(disassemble(b));
    } else if (rw == "reload") {
        // update all
        for (var i = 0; i < 256; i++) {
            $("#dis_h_" + i).html(hex_byte(mem2(i)));
            $("#dis_d_" + i).html(disassemble(mem2(i)));
        }
    }
}

// Copy memory to editor and switch to it.
function switch_to_editor() {
    var memstring = "";
    for (var i = 0; i < 256; i++) {
        var b = hex_byte(mem2(i));
        memstring += b;
        memstring += " ";
        if ((i % 16 == 15) && i < 255) {
            memstring += "\n";
        }
    }

    $("#memory_editor").val(memstring);
    $("#mainpage").hide();
    $("#editmem").show();
}

// Parse memory editor changes and accept if valid.
function memedit_accept() {
    clearError(); 
    var text = $("#memory_editor").val();
    var buf = [];
    var i = 0; // current index into buf
    var c = 0; // numberical value of current character
    var cur = ""; // current character

    for (var index = 0; index < text.length; index++) {
        var ch = text[index];
        if (cur == "") {
            // expecting first char. or whitespace
            if (ch == " " || ch == "\n") { continue; }
            var x = HEX.indexOf(ch);
            if (x > -1) {
                cur = ch;
                c = x << 4; // high nibble
            } else {
                reportError("Wasn't expecting '" + ch + "' at index " + i + "."); 
                return;
            }
        } else {
            // expecting the second half of a character.
            var x = HEX.indexOf(ch);
            if (x > -1) {
                cur += ch;
                c += x;
                buf[i] = c;
                i++;
                if (i > 256) {
                    reportError("Overflow, max. 256 bytes.");
                    return;
                }
                c = 0;
                cur = "";
            } else {
                reportError("Wasn't expecting '" + ch + "' at index " + i + "."); 
                return;
            }
        }
    }
    mem_update(buf);
    memedit_cancel();    
    return;
}

// Cancel memory editor changes.
function memedit_cancel() {
    $("#mainpage").show();
    $("#editmem").hide();
}

// current delay.
function get_delay() {
    var delay = $("#delay").val();
    var d;
    if (delay == "10ms") { d = 10; }
    else if (delay == "20ms") { d = 20; }
    else if (delay == "50ms") { d = 50; }
    else if (delay == "100ms") { d = 100; }
    else if (delay == "200ms") { d = 200; }
    else if (delay == "500ms") { d = 500; }
    else { d = 1000; }
    return d;
}

// Hit when a register updates. If it's the pc and we're running, highlight
// the next instruction.
function update_pc(n, v) {
    if (n == "p") {
        $("#disassembly").find('.dis_cell').removeClass('current_pc');
        if (is_running()) {
            $("#dis_d_" + v).addClass('current_pc');
        }
    } else if (n == "run") {
        if (v) {
            $("#dis_d_" + rp()).addClass('current_pc');
        } else {
            $("#disassembly").find('.dis_cell').removeClass('current_pc');
        }
    }
    
}

// return the current breakpoints.
function breakpoints() {
    var breaks = [];
    for (var i = 0; i < 256; i++) {
        if($("#dis_b_" + i).is(':checked')) {
            breaks[i] = 1;
        }
    }
    return breaks;
}

/* emulator */

var ISA = [
    { name: 'LDAM', opcode: 0, run: function() { ra(mem(ro())); ro(0); } },
    { name: 'LDBM', opcode: 1, run: function() { rb(mem(ro())); ro(0); } },
    { name: 'STAM', opcode: 2, run: function() { mem(ro(), ra()); ro(0); } },
    { name: 'LDAC', opcode: 3, run: function() { ra(ro()); ro(0); } },
    { name: 'LDBC', opcode: 4, run: function() { rb(ro()); ro(0); } },
    { name: 'LDAP', opcode: 5, run: function() { ra(rp()+ro()); ro(0); } },
    { name: 'LDAI', opcode: 6, run: function() { ra(mem(ra()+ro())); ro(0); } },
    { name: 'LDBI', opcode: 7, run: function() { rb(mem(rb()+ro())); ro(0); } },
    { name: 'STAI', opcode: 8, run: function() { mem(rb()+ro(),ra()); ro(0);} },
    { name: 'BR'  , opcode: 9, run: function() { if (ro() == 254) { hlt(); }
                                                 else {rp(rp()+ro());} ro(0);}},
    { name: 'BRZ' , opcode:10, run: function() { if (ra()==0) {rp(rp()+ro());}
                                                 ro(0);} },
    { name: 'BRN' , opcode:11, run: function() { if (ra()>127) {rp(rp()+ro());}
                                                 ro(0);} },
    { name: 'BRB' , opcode:12, run: function() { rp(rb()); ro(0); } },
    { name: 'ADD' , opcode:13, run: function() { ra(ra()+rb()); ro(0); } },
    { name: 'SUB' , opcode:14, run: function() { ra(ra()-rb()); ro(0); } },
    { name: 'PFIX', opcode:15, run: function() { ro(ro() << 4); } }
];

function is_running() {
    return model.running;
}

function reset() {
    rp(0); ra(0); rb(0); ro(0);
    model.running = 1;
    for (var i = 0; i < reg_observers.length; i++) {
        (reg_observers[i])("run", 1);
    }
}

// halt the simulation.
function hlt() {
    model.running = 0;
    for (var i = 0; i < reg_observers.length; i++) {
        (reg_observers[i])("run", 0);
    }
}

function step() {
    if (!model.running) {
        
        return;
    }

    var pc = rp();
    var inst = mem(pc);
    var opcode = (inst >> 4);
    var operand = inst % 16;
    var operation = ISA[opcode];
    
    ro((ro() & 240) | operand);
    rp(rp() + 1);
    operation.run();
}

function disassemble(inst) {
    var opcode = inst >> 4;
    var opname = ISA[opcode].name;
    if (opname.length < 4) { opname += " " }
    if (opname.length < 4) { opname += " " }
    var operand = inst % 16;
    return opname + "  " + HEX[operand];
}

function run() {
    if (is_running()) {
        var bps = breakpoints();

        // TODO speed
        // TODO way to interrupt
        step();
        if (bps[rp()]) {
            return;
        }
        setTimeout(run, get_delay());
    }
}

/* loader */

$(function(){
    init_model(model);

    init_disassembly_view();

    init_memory_view();

    $("#R").click(function(){
        clearError();
        var addr = $("#address").val();
        var value = $("#value").val();
        if (!is_byte(addr)) {
            reportError("Check your values.");
        } else {
            mem(addr);
        }
    });

    $("#W").click(function(){
        clearError();
        var addr = $("#address").val();
        var value = $("#value").val();
        if (!is_byte(addr) || !is_byte(value)) {
            reportError("Check your values.");
        } else {
            mem(addr, value);
        }
    });

    reg_observers.push(update_registers);
    reg_observers.push(update_pc);
    reg_observers.push(update_control);

    $("#reset").click(reset);
    $("#step").click(step);
    $("#run").click(run);
    update_control("run", 0);

    $("#load").click(switch_to_editor);
    $("#editmem").hide();

    $("#edityes").click(memedit_accept);
    $("#editno").click(memedit_cancel);

    $("#messages").hide();
    $("#clearmessage").click(function() {$("#messages").hide()});
});

