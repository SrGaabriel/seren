
%Vector = type { i32, i32, i32, i16, i16, i16, i8 }
@str_-1279675545 = unnamed_addr constant [6 x i8] c"X: %d\00"
@str_47805983 = unnamed_addr constant [6 x i8] c"Y: %d\00"
@str_232328991 = unnamed_addr constant [6 x i8] c"Z: %d\00"
@str_2043898602 = unnamed_addr constant [6 x i8] c"W: %d\00"
@str_-1898879192 = unnamed_addr constant [6 x i8] c"A: %d\00"
@str_933047538 = unnamed_addr constant [6 x i8] c"B: %d\00"
@str_748593679 = unnamed_addr constant [6 x i8] c"L: %d\00"
declare void @printf(i8*, ...)

define i32 @main() {
  %reg_1 = alloca %Vector, align 4
  store %Vector { i32 20, i32 19, i32 17, i16 18, i16 15, i16 14, i8 16 }, %Vector* %reg_1
  %reg_4 = getelementptr [6 x i8], [6 x i8]* @str_-1279675545, i32 0
  %reg_6 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 0
  %reg_8 = load i32, i32* %reg_6
  call void @printf(i8* %reg_4, i32 %reg_8)
  %reg_11 = getelementptr [6 x i8], [6 x i8]* @str_47805983, i32 0
  %reg_13 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 1
  %reg_15 = load i32, i32* %reg_13
  call void @printf(i8* %reg_11, i32 %reg_15)
  %reg_18 = getelementptr [6 x i8], [6 x i8]* @str_232328991, i32 0
  %reg_20 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 3
  %reg_22 = load i16, i16* %reg_20
  call void @printf(i8* %reg_18, i16 %reg_22)
  %reg_25 = getelementptr [6 x i8], [6 x i8]* @str_2043898602, i32 0
  %reg_27 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 2
  %reg_29 = load i32, i32* %reg_27
  call void @printf(i8* %reg_25, i32 %reg_29)
  %reg_32 = getelementptr [6 x i8], [6 x i8]* @str_-1898879192, i32 0
  %reg_34 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 6
  %reg_36 = load i8, i8* %reg_34
  call void @printf(i8* %reg_32, i8 %reg_36)
  %reg_39 = getelementptr [6 x i8], [6 x i8]* @str_933047538, i32 0
  %reg_41 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 4
  %reg_43 = load i16, i16* %reg_41
  call void @printf(i8* %reg_39, i16 %reg_43)
  %reg_46 = getelementptr [6 x i8], [6 x i8]* @str_748593679, i32 0
  %reg_48 = getelementptr inbounds %Vector, %Vector* %reg_1, i32 0, i32 5
  %reg_50 = load i16, i16* %reg_48
  call void @printf(i8* %reg_46, i16 %reg_50)
  ret i32 0
}
