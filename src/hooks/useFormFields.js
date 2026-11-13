import { useState, useDebugValue } from "react";

function useFormFields(initialValues) {
  const [values, setValues] = useState(initialValues);

  useDebugValue(values);

  function handleChange(event) {
    const { name, value } = event.target;
    setValues((prev) => ({ ...prev, [name]: value }));
  }

  function reset() {
    setValues(initialValues);
  }

  return { values, setValues, handleChange, reset };
}

export default useFormFields;
