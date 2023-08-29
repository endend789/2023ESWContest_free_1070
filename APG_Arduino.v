#define ts 0.001 * 0.001
#include <SoftwareSerial.h>

int RX = 8;
int TX = 7;
SoftwareSerial bluetooth(RX, TX);

float PulseSensorPurplePin = 0;
unsigned long tCount = 0;
unsigned long tCountPre = 0;

const float a2 = 0.7265;
const float b1 = 0.1367;
const float b2 = 0.1367;
int a = 0;
int b = 0;
int c = 0;
int d = 0;
int quality = 0;
int chk = -1;
int forloop = 0;
float zero_crossing[16] = {0,};
float sum_zero_crossing = 0;
float sum_array_zero_crossing[7] = {0,};
float average_zero_crossing[7] = {0,};
float positiveThres = 0;
float max1 = 0;
float max2 = 0;
float max3 = 0;
float y = 0;
float diff_y_pre;
float filtered_y_pre;
float diff_y = 0;
float diff2_y = 0;
float dt = 0;
float y_pre = 0;
float filtered_y = 0;
float filtered_diff_y = 0;
float filtered_diff_pre = 0;
float filtered_diff_pre_for_zerocrossing = 0;
float filtered_diff2_pre = 0;
float filtered_diff2_y = 0;
float diff2_y_pre = 0;
float max = 0;
float min = 0;
float flexibility = 0;
float average = 0;
float MAX[16] = {0,};
float MIN[16] = {0,};
float Flex[16] = {0,};

void setting() {
  tCount = micros();
  y = analogRead(PulseSensorPurplePin);
  dt = (float)(tCount - tCountPre) * ts;
}

void saveStates() {
  filtered_y_pre = filtered_y;
  y_pre = y;
  tCountPre = tCount;
  filtered_y_pre = filtered_y;
  filtered_diff_pre_for_zerocrossing = filtered_diff_pre;
  filtered_diff_pre = filtered_diff_y;
  diff_y_pre = diff_y;
  filtered_diff2_pre = filtered_diff2_y;
  diff2_y_pre = diff2_y;
}
void LPF_raw() {
  filtered_y = a2 * filtered_y_pre + b1 * y + b2 * y_pre;
}
void firstDerivative() {
  diff_y = (filtered_y - filtered_y_pre) / dt;
}
void LPF_1() {
  filtered_diff_y = a2 * filtered_diff_pre + b1 * diff_y + b2 * diff_y_pre;
}
void secondDerivative() {
  diff2_y = (filtered_diff_y - filtered_diff_pre) / dt;
}
void LPF_2() {
  filtered_diff2_y = a2 * filtered_diff2_pre + b1 * diff2_y + b2 * diff2_y_pre;
}
void updateMaxwithThres() {
  if (filtered_diff2_y > positiveThres && max < filtered_diff2_y)
  {
    max = filtered_diff2_y;
  }
}
void getData() {
  setting();
  LPF_raw();
  firstDerivative();
  LPF_1();
  secondDerivative();
  LPF_2();
  saveStates();
}
void setFirstThreshold() { //최초 threshold 설정을 위한 루프
  while (b < 4) //b=세트 수
  {
    a = 0;
    max = 0;
    min = 0;
    while (a < 100) //100개 중에 최대값 3개 얻음
    {
      getData();
      if (b > 0) {  //한 세트(b=0) 날리기
        delay(5);
        bluetooth.println(filtered_diff2_y);
        if (max < filtered_diff2_y) {
          max = filtered_diff2_y;
        }
      }
      delay(5);
      a++;
    }
    positiveThres = positiveThres + max;
    b++;
  }
  max1 = positiveThres / 3;
  max2 = positiveThres / 3;
  max3 = positiveThres / 3;

  positiveThres = (positiveThres / 3) * 0.7;
}

void adjustPositiveThreshold() {
  max1 = max2;
  max2 = max3;
  max3 = max;
  positiveThres = 0.7 * (max1 + max2 + max3) / 3;
}

void setup()
{
  a = 0;
  c = 0;
  Serial.begin(9600);
  bluetooth.begin(9600);
label1:
  int start = 0;
  int end=0;
  a = 0;
  b = 0;
  c = 0;
  d = 0;
  quality = 0;
  chk = -1;
  forloop = 0;
  sum_zero_crossing = 0;
  sum_array_zero_crossing[7] = {0,};
  average_zero_crossing[7] = {0,};
  positiveThres = 0;
  max1 = 0;
  max2 = 0;
  max3 = 0;
  y = 0;
  diff_y_pre = 0;
  filtered_y_pre = 0;
  diff_y = 0;
  diff2_y = 0;
  dt = 0;
  y_pre = 0;
  filtered_y = 0;
  filtered_diff_y = 0;
  filtered_diff_pre = 0;
  filtered_diff_pre_for_zerocrossing = 0;
  filtered_diff2_pre = 0;
  filtered_diff2_y = 0;
  diff2_y_pre = 0;
  max = 0;
  min = 0;
  flexibility = 0;
  average = 0;
  MAX[16] = {0,};
  MIN[16] = {0,};
  Flex[16] = {0,};
  for (int i = 0; i < 16; i++)
  {
    zero_crossing[i] = 0;
  }

  //  Serial.print("chk1: ");
  //  Serial.println(chk);
  //  Serial.print("forloop2: ");
  //  Serial.println(forloop);
  //  Serial.print("forloop3: ");
  //  Serial.println(forloop);
  while (forloop < 9)
  {
    chk = (int)bluetooth.read();
    //    Serial.print("chk: ");
    //    Serial.println(chk);
    if (chk == 49)
    {
      Serial.println("on");
      //      Serial.print("chk2: ");
      //      Serial.println(chk);
      forloop = 15;
      bluetooth.println(3333333);
    }
    else
    {
      //      Serial.print("chk3: ");
      //      Serial.println(chk);
      //      Serial.println("off");
      forloop = 3;
    }
  }
  Serial.println("end");
  y = analogRead(PulseSensorPurplePin);
  start=millis();
  setFirstThreshold();
  Serial.print("positiveThres: ");
  Serial.println(positiveThres);
  b = 0;
  a = 0;
  c = 0;
  while (a < 1000) //첫 a peak 찾기
  {
    getData();
    if (filtered_diff2_y > positiveThres)
    {
      while (b < 18) // b = max, min 세트 수 + 2
      {
        c = 0;
        max = 0;
        min = 0;
        while (c < 400) // 주기별로 자르기, c=세트당 개수
        {
          getData();
          if (b > 0) //한 세트(b=0) 날리기
          {
            updateMaxwithThres();
            if (b > 1)
            {
              bluetooth.println(filtered_diff2_y);
              //              Serial.println(filtered_diff2_y);
              if (filtered_diff_pre_for_zerocrossing * filtered_diff_y < 0) //zero crossing 개수 세기
              {
                zero_crossing[b - 2]++; //b가 2일때부터 zero crossing 카운트 해서 b-2(배열 인덱스 0부터)
              }
            }
            if (filtered_diff2_y < min)
            {
              min = filtered_diff2_y;
            }
            if (c > 40 && filtered_diff2_y > positiveThres)
            {
              c = 1000;
            }
          } // 한 세트 데이터(max, min) 저장, max min 검출 끝
          delay(5);
          c++;
        }
        if (b > 0) //max나 min에 0 있을 때 날리기
        {
          if (min == 0) // b/a가 불가하면 세트 한번 더 돌리기
          {
            b--;
          }
          else // 정상적인 세트 일 때(min != 0), b/a 연산 진행
          {
            if (b > 1) // b가 1 초과일때부터 계산(결론적으로 2개 세트 날림)
            {
              adjustPositiveThreshold();
              flexibility = flexibility + min / max;
              MAX[b - 2] = max;
              MIN[b - 2] = min;
              Flex[b - 2] = flexibility;
            }
          }
        }
        if (b >= 11)
        {
          sum_zero_crossing = 0; // 수정 필요 시작점
          for (int k = 11; k >= 2; k--)
          {
            sum_zero_crossing = sum_zero_crossing + zero_crossing[b - k];
          }
          sum_array_zero_crossing[b - 11] = sum_zero_crossing; // 수정 필요 끝점
          average_zero_crossing[b - 11] = sum_zero_crossing / 10;
        }
        b++;
        if (end-start > 60000)
        {
          quality = 5;
          goto label2;
        }
      }
    }
    if (b == 17) // b=세트 수 +1
    {
      break;
    }
    a++;
    delay(5);
  }
  average = flexibility / 16; //분모: 세트 수
  for (int i = 0; i < 16; i++)
  {
    if (zero_crossing[i] != 2 && zero_crossing[i] != 4 && zero_crossing[i] != 6)
    {
      quality++;
    }
  }
  if (average < -1.56 || average > -0.52)
  {
    quality = 5;
  }
label2:
  if (quality < 3)
  {
    bluetooth.println(1111111);
    Serial.print("average: ");
    Serial.println(average);
    bluetooth.println(average);
    for (int i = 0; i < 16; i++)
    {
      Serial.println(zero_crossing[i]);
    }
    Serial.println(millis());
  }
  else
  {
    bluetooth.println(2222222);
    Serial.println("재측정 필요");
    Serial.println(quality);
    Serial.println(millis());
  }
  //  Serial.print("chk4: ");
  //  Serial.println(chk);
  chk = 0;
  //  Serial.print("forloop2: ");
  //  Serial.println(forloop);
  goto label1;


  //  for (int i = 0; i < 16; i++)
  //  {
  //    Serial.println(Flex[i]);
  //  }
  //  for (int i = 0; i < 16; i++)
  //  {
  //    Serial.println(MAX[i]);
  //  }
  //  for (int i = 0; i < 16; i++)
  //  {
  //    Serial.println(MIN[i]);
  //  }
  //  Serial.println("");
}

void loop() {

}