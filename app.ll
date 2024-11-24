
%Vector = type { i32, i32, i32, i16, i16, i16, i8 }
@str_-1109425166 = unnamed_addr constant [7 x i8] c"X: %d
\00"
@str_-138130345 = unnamed_addr constant [7 x i8] c"Y: %d
\00"
@str_1730087261 = unnamed_addr constant [7 x i8] c"Z: %d
\00"
@str_459886535 = unnamed_addr constant [7 x i8] c"W: %d
\00"
@str_-1481364697 = unnamed_addr constant [7 x i8] c"A: %d
\00"
@str_-1133231185 = unnamed_addr constant [7 x i8] c"B: %d
\00"
@str_1665828087 = unnamed_addr constant [7 x i8] c"L: %d
\00"
declare void @printf(i8*, ...)

define i32 @main() {
  %reg_1 = alloca %Vector, align 4
  store %Vector { i32 20, i32 19, i32 17, i16 18, i16 15, i16 14, i8 16 }, %Vector* %reg_1
  %reg_4 = bitcast [7 x i8]* @str_-1109425166 to i8*
  %reg_6 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 0
  %reg_8 = load i32, i32* %reg_6
  call void @printf(i8* %reg_4, i32 %reg_8)
  %reg_11 = bitcast [7 x i8]* @str_-138130345 to i8*
  %reg_13 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 1
  %reg_15 = load i32, i32* %reg_13
  call void @printf(i8* %reg_11, i32 %reg_15)
  %reg_18 = bitcast [7 x i8]* @str_1730087261 to i8*
  %reg_20 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 3
  %reg_22 = load i16, i16* %reg_20
  call void @printf(i8* %reg_18, i16 %reg_22)
  %reg_25 = bitcast [7 x i8]* @str_459886535 to i8*
  %reg_27 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 2
  %reg_29 = load i32, i32* %reg_27
  call void @printf(i8* %reg_25, i32 %reg_29)
  %reg_32 = bitcast [7 x i8]* @str_-1481364697 to i8*
  %reg_34 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 6
  %reg_36 = load i8, i8* %reg_34
  call void @printf(i8* %reg_32, i8 %reg_36)
  %reg_39 = bitcast [7 x i8]* @str_-1133231185 to i8*
  %reg_41 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 4
  %reg_43 = load i16, i16* %reg_41
  call void @printf(i8* %reg_39, i16 %reg_43)
  %reg_46 = bitcast [7 x i8]* @str_1665828087 to i8*
  %reg_48 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 5
  %reg_50 = load i16, i16* %reg_48
  call void @printf(i8* %reg_46, i16 %reg_50)
  ret i32 0
}
