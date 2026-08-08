// Enter your pattern code here and click on the Play button
// below to execute your code. The results will then be visible
// in the Pattern Data view.
// 
// More information can be found in the documentation.
// 
// 
// Simple example:
// 
// import std.io;
// 
// struct Pattern {
//     u32 int;
//     float floating_point;
// };
// 
// Pattern my_pattern @ 0x00;
// std::print("0x{:08X}", my_pattern.int);

#include <type/leb128.pat>
#include <std/mem.pat>

struct Name {
    type::uLEB128 length;
    char s[length];
};

struct Section {
    u8 id;
    type::uLEB128 len;
    std::mem::Bytes<len> bytes;
};

struct Wasm {
    be u32 magic;
    le u32 version;
    
    Section sections[while(!std::mem::eof())];
};

Wasm wasm @ 0;