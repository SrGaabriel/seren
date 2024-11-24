
%Vector = type { i32, i32, i32, i16, i16, i16, i8, i8* }
@str_-872991654 = unnamed_addr constant [7 x i8] c"X: %d
\00"
@str_-609879893 = unnamed_addr constant [7 x i8] c"Y: %d
\00"
@str_-779476004 = unnamed_addr constant [7 x i8] c"Z: %d
\00"
@str_-274815587 = unnamed_addr constant [7 x i8] c"W: %d
\00"
@str_-1684670759 = unnamed_addr constant [7 x i8] c"A: %d
\00"
@str_-2012208845 = unnamed_addr constant [7 x i8] c"B: %d
\00"
@str_-60529565 = unnamed_addr constant [7 x i8] c"L: %d
\00"
@str_1031801007 = unnamed_addr constant [12 x i8] c"Planet: %s
\00"
declare void @printf(i8*, ...)

define i32 @main() {
  %reg_1 = alloca %Vector, align 1
  store %Vector { i32 20, i32 19, i32 17, i16 18, i16 15, i16 14, i8 16 }, %Vector* %reg_1
  call void @print_vector(%Vector* %reg_1)
  ret i32 0
}
define void @print_vector(%Vector* %reg_1) {
  %reg_2 = bitcast [7 x i8]* @str_-872991654 to i8*
  %reg_4 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 0
  %reg_6 = load i32, i32* %reg_4
  call void @printf(i8* %reg_2, i32 %reg_6)
  %reg_9 = bitcast [7 x i8]* @str_-609879893 to i8*
  %reg_11 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 1
  %reg_13 = load i32, i32* %reg_11
  call void @printf(i8* %reg_9, i32 %reg_13)
  %reg_16 = bitcast [7 x i8]* @str_-779476004 to i8*
  %reg_18 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 3
  %reg_20 = load i16, i16* %reg_18
  call void @printf(i8* %reg_16, i16 %reg_20)
  %reg_23 = bitcast [7 x i8]* @str_-274815587 to i8*
  %reg_25 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 2
  %reg_27 = load i32, i32* %reg_25
  call void @printf(i8* %reg_23, i32 %reg_27)
  %reg_30 = bitcast [7 x i8]* @str_-1684670759 to i8*
  %reg_32 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 6
  %reg_34 = load i8, i8* %reg_32
  call void @printf(i8* %reg_30, i8 %reg_34)
  %reg_37 = bitcast [7 x i8]* @str_-2012208845 to i8*
  %reg_39 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 4
  %reg_41 = load i16, i16* %reg_39
  call void @printf(i8* %reg_37, i16 %reg_41)
  %reg_44 = bitcast [7 x i8]* @str_-60529565 to i8*
  %reg_46 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 5
  %reg_48 = load i16, i16* %reg_46
  call void @printf(i8* %reg_44, i16 %reg_48)
  %reg_51 = bitcast [12 x i8]* @str_1031801007 to i8*
  %reg_53 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 7
  %reg_55 = load i8*, i8** %reg_53
  call void @printf(i8* %reg_51, i8* %reg_55)
  ret void 
}
