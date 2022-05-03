import React from "react";
import { Box, Stack, TextField, IconButton, Grid, Button } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import styles from "./styles";

interface IMyprops {
  requestBody: any;
  formData: any;
  setFormData: (formDate: any) => void;
  onSubmit: (a: any) => void;
}

export const Request: React.FC<IMyprops> = (props: IMyprops) => {
  const myRequest = props.requestBody;
  const formData = props.formData;
  const setter = props.setFormData;
  const onSubmit = props.onSubmit;

  const handleChange = (key: string, v: string) => {
    setter({
      ...formData,
      [key]: v,
    });
  };

  const clearValue = (key: string) => {
    setter({
      ...formData,
      [key]: "",
    });
  };

  return (
    <>
      {props.requestBody ? (
        <>
          <Box>
            {Object.keys(myRequest).map((key: string) =>
              myRequest[key].map((re: any, index: string) => {
                return (
                  <Grid
                    container
                    rowSpacing={2}
                    columnSpacing={3}
                    sx={{ width: "100%", marginTop: 1 }}
                    alignItems="center"
                    key={index}
                  >
                    <Grid item xs={5}>
                      <Stack direction="row" spacing={1} sx={{ width: "40%" }}>
                        <div style={{ width: "100%", whiteSpace: "normal" }}>
                          {re.value && (
                            <div style={styles.apiTitle}>{re.value}</div>
                          )}
                          {re.type && (
                            <div style={styles.apiType}>{re.type}</div>
                          )}
                        </div>
                      </Stack>
                    </Grid>
                    <Grid item xs={1.5}>
                      {re.required ? (
                        <div style={styles.apiRequired.required}>required</div>
                      ) : (
                        <div style={styles.apiRequired.notRequired}>
                          not required
                        </div>
                      )}
                    </Grid>
                    <Grid item xs={5.5}>
                      <TextField
                        label={re.value}
                        size="small"
                        type="text"
                        sx={{ width: "100%" }}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                          handleChange(re.value, e.target.value);
                        }}
                        value={formData[re.value] || ""}
                        InputProps={{
                          endAdornment: (
                            <IconButton
                              size="small"
                              onClick={() => clearValue(re.value)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          ),
                        }}
                      ></TextField>
                    </Grid>
                  </Grid>
                );
              })
            )}
          </Box>{" "}
          <Button variant="outlined" color="error" onClick={() => setter({})}>
            입력값 초기화
          </Button>
          <Button
            variant="contained"
            type="button"
            color="primary"
            onClick={() => onSubmit(formData)}
          >
            출력값 확인하기
          </Button>
        </>
      ) : null}
    </>
  );
};
