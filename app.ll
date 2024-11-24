
%Vector = type { i32, i32, i32, i16, i16, i16, i8 }
@str_-1410008837 = unnamed_addr constant [6 x i8] c"X: %d\00"
@str_-1771611947 = unnamed_addr constant [6 x i8] c"Y: %d\00"
@str_-1740081982 = unnamed_addr constant [6 x i8] c"Z: %d\00"
@str_518975329 = unnamed_addr constant [6 x i8] c"W: %d\00"
@str_-33077808 = unnamed_addr constant [6 x i8] c"A: %d\00"
@str_1148222761 = unnamed_addr constant [6 x i8] c"B: %d\00"
@str_1465409005 = unnamed_addr constant [6 x i8] c"L: %d\00"
declare void @printf(i8*, ...)

define i32 @get_x(%Vector* %reg_1) {
  %reg_2 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 0
  %reg_4 = load i32, i32** %reg_2
  ret i32 %reg_4
}
define i32 @main() {
  %reg_1 = add i32 5, 0
  %reg_3 = alloca %Vector, align 4
  store %Vector { i32 20, i32 19, i32 17, i16 18, i16 15, i16 14, i8 16 }, %Vector* %reg_3
  %reg_6 = getelementptr [6 x i8], [6 x i8]* @str_-1410008837, i32 0
  %reg_8 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 0
  %reg_10 = load i32, i32** %reg_8
  call void @printf(i8* %reg_6, i32 %reg_10)
  %reg_13 = getelementptr [6 x i8], [6 x i8]* @str_-1771611947, i32 0
  %reg_15 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 1
  %reg_17 = load i32, i32** %reg_15
  call void @printf(i8* %reg_13, i32 %reg_17)
  %reg_20 = getelementptr [6 x i8], [6 x i8]* @str_-1740081982, i32 0
  %reg_22 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 3
  %reg_24 = load i16, i16** %reg_22
  call void @printf(i8* %reg_20, i16 %reg_24)
  %reg_27 = getelementptr [6 x i8], [6 x i8]* @str_518975329, i32 0
  %reg_29 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 2
  %reg_31 = load i32, i32** %reg_29
  call void @printf(i8* %reg_27, i32 %reg_31)
  %reg_34 = getelementptr [6 x i8], [6 x i8]* @str_-33077808, i32 0
  %reg_36 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 6
  %reg_38 = load i8, i8** %reg_36
  call void @printf(i8* %reg_34, i8 %reg_38)
  %reg_41 = getelementptr [6 x i8], [6 x i8]* @str_1148222761, i32 0
  %reg_43 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 4
  %reg_45 = load i16, i16** %reg_43
  call void @printf(i8* %reg_41, i16 %reg_45)
  %reg_48 = getelementptr [6 x i8], [6 x i8]* @str_1465409005, i32 0
  %reg_50 = getelementptr inbounds %Vector, %Vector* %reg_3, i32 0, i32 5
  %reg_52 = load i16, i16** %reg_50
  call void @printf(i8* %reg_48, i16 %reg_52)
  ret i32 0
}
define void @print_line(i8* %reg_1) {
  call void @printf(i8* %reg_1)
  ret void 
}
define i32 @test() {
  ret i32 6
}
