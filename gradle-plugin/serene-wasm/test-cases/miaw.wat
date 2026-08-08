(module
    (type $u (func (param i32)))
    (type $gotten (func (param i32) (result i32)))

    (import "env" "get_fn" (func $get_fn (result funcref)))

    (func $main (type $u)
        call $get_fn
        i32.const 69
        call_ref $gotten
        return
    )

    (start $main)
)
