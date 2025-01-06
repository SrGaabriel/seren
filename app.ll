
%enum_Option = type { i8, ptr }
%Vector = type { i32, i32, i32, i16, i16, i16, i8, i8* }
@str_822363430=linkonce_odr unnamed_addr constant [6 x i8] c"Earth\00"
@str_-1906521799=linkonce_odr unnamed_addr constant [26 x i8] c"20+20 does not equal %d!
\00"
@str_2086508457=linkonce_odr unnamed_addr constant [25 x i8] c"20+20 indeed equals %d!
\00"
@str_306700505=linkonce_odr unnamed_addr constant [7 x i8] c"X: %d
\00"
@str_2056117156=linkonce_odr unnamed_addr constant [7 x i8] c"Y: %d
\00"
@str_-479437791=linkonce_odr unnamed_addr constant [7 x i8] c"Z: %d
\00"
@str_-2145447212=linkonce_odr unnamed_addr constant [7 x i8] c"W: %d
\00"
@str_1165070256=linkonce_odr unnamed_addr constant [7 x i8] c"A: %d
\00"
@str_551916931=linkonce_odr unnamed_addr constant [7 x i8] c"B: %d
\00"
@str_1357459580=linkonce_odr unnamed_addr constant [7 x i8] c"L: %d
\00"
@str_-238341687=linkonce_odr unnamed_addr constant [12 x i8] c"Planet: %s
\00"
declare void @printf(i8*, ...)

define i32 @main() {
entry:
  %reg_1 = alloca %Vector, align 1
  %reg_3 = bitcast [6 x i8]* @str_822363430 to i8*
  %reg_5 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 0
  store i32 20, i32* %reg_5
  %reg_8 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 1
  store i32 19, i32* %reg_8
  %reg_11 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 2
  store i32 17, i32* %reg_11
  %reg_14 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 3
  store i16 18, i16* %reg_14
  %reg_17 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 4
  store i16 15, i16* %reg_17
  %reg_20 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 5
  store i16 14, i16* %reg_20
  %reg_23 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 6
  store i8 16, i8* %reg_23
  %reg_26 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 7
  store i8* %reg_3, i8** %reg_26
  %reg_29 = add i32 40, 0
  %reg_31 = add i32 20, 20
  %reg_33 = icmp eq i32 %reg_31, %reg_29
  br i1 %reg_33, label %block_0, label %block_1
block_0:
  %reg_40 = bitcast [25 x i8]* @str_2086508457 to i8*
  call void @printf(i8* %reg_40, i32 %reg_29)
  ret i32 0
  br label %block_2
block_1:
  %reg_36 = bitcast [26 x i8]* @str_-1906521799 to i8*
  call void @printf(i8* %reg_36, i32 %reg_29)
  br label %block_2
block_2:
  call void @print_vector(%Vector* %reg_1)
  ret i32 0

}
define void @print_vector(%Vector* %reg_1) {
entry:
  %reg_2 = bitcast [7 x i8]* @str_306700505 to i8*
  %reg_4 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 0
  %reg_6 = load i32, i32* %reg_4
  call void @printf(i8* %reg_2, i32 %reg_6)
  %reg_9 = bitcast [7 x i8]* @str_2056117156 to i8*
  %reg_11 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 1
  %reg_13 = load i32, i32* %reg_11
  call void @printf(i8* %reg_9, i32 %reg_13)
  %reg_16 = bitcast [7 x i8]* @str_-479437791 to i8*
  %reg_18 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 3
  %reg_20 = load i16, i16* %reg_18
  call void @printf(i8* %reg_16, i16 %reg_20)
  %reg_23 = bitcast [7 x i8]* @str_-2145447212 to i8*
  %reg_25 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 2
  %reg_27 = load i32, i32* %reg_25
  call void @printf(i8* %reg_23, i32 %reg_27)
  %reg_30 = bitcast [7 x i8]* @str_1165070256 to i8*
  %reg_32 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 6
  %reg_34 = load i8, i8* %reg_32
  call void @printf(i8* %reg_30, i8 %reg_34)
  %reg_37 = bitcast [7 x i8]* @str_551916931 to i8*
  %reg_39 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 4
  %reg_41 = load i16, i16* %reg_39
  call void @printf(i8* %reg_37, i16 %reg_41)
  %reg_44 = bitcast [7 x i8]* @str_1357459580 to i8*
  %reg_46 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 5
  %reg_48 = load i16, i16* %reg_46
  call void @printf(i8* %reg_44, i16 %reg_48)
  %reg_51 = bitcast [12 x i8]* @str_-238341687 to i8*
  %reg_53 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 7
  %reg_55 = load i8*, i8** %reg_53
  call void @printf(i8* %reg_51, i8* %reg_55)
  ret void 

}
