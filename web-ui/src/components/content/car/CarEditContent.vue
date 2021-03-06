<template>
  <div>
    <form novalidate @submit.prevent="validateCar">

      <md-card>
        <md-card-content>
          <md-field :class="getValidationClass('number')">
            <label>Number</label>
            <md-input
              v-model="number"
              name="number"
              :disabled="sending"
              :maxlength="fieldsLength.CAR_NUMBER"
            />
            <span
              class="md-error"
              v-if="!$v.number.required"
            >
              {{errors.FIELD_IS_REQUIRED}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.number.maxlength"
            >
              {{errors.MAX_LENGTH(fieldsLength.CAR_NUMBER)}}
            </span>
          </md-field>
          <md-field :class="getValidationClass('fuelConsumption')">
            <label>Fuel consumption</label>
            <md-input
              v-model="fuelConsumption"
              name="fuelConsumption"
              :disabled="sending"
            />
            <span
              class="md-error"
              v-if="!$v.fuelConsumption.required"
            >
              {{errors.FIELD_IS_REQUIRED}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.fuelConsumption.decimal"
            >
              {{errors.INCORRECT_TYPE(fieldsType.CAR_FUEL_CONSUMPTION)}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.fuelConsumption.between"
            >
              {{errors.INCORRECT_VALUE(fieldsValueBounds.CAR_FUEL_CONSUMPTION.min, fieldsValueBounds.CAR_FUEL_CONSUMPTION.max)}}
            </span>
          </md-field>
          <md-field :class="getValidationClass('loadCapacity')">
            <label>Load capacity</label>
            <md-input
              v-model="loadCapacity"
              name="loadCapacity"
              :disabled="sending"
            />
            <span
              class="md-error"
              v-if="!$v.loadCapacity.required"
            >
              {{errors.FIELD_IS_REQUIRED}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.loadCapacity.integer"
            >
              {{errors.INCORRECT_TYPE(fieldsType.CAR_LOAD_CAPACITY)}}
            </span>
            <span
              class="md-error"
              v-else-if="!$v.loadCapacity.between"
            >
              {{errors.INCORRECT_VALUE(fieldsValueBounds.CAR_LOAD_CAPACITY.min, fieldsValueBounds.CAR_LOAD_CAPACITY.max)}}
            </span>
          </md-field>
          <md-field>
            <label>Car type</label>
            <md-select
              v-model="carType"
              :disabled="sending"
            >
              <md-option value="CISTERN">Cistern</md-option>
              <md-option value="REFRIGERATOR">Refrigerator</md-option>
              <md-option value="COVERED_BODY">Covered body</md-option>
            </md-select>
          </md-field>
        </md-card-content>

        <md-progress-bar md-mode="indeterminate" v-if="sending"/>

        <md-card-actions>
          <md-button
            :disabled="sending"
            to="/cars"
          >
            Back
          </md-button>

          <md-button
            type="submit"
            class="md-primary"
            :disabled="sending"
          >
            Edit
          </md-button>
        </md-card-actions>

        <md-chip
          class="md-accent md-layout md-alignment-center"
          v-if="hasError"
        >
          {{errorMessage}}
        </md-chip>

      </md-card>

      <md-snackbar
        :md-active.sync="carUpdated"
      >
        {{messages.CAR_UPDATED}}
      </md-snackbar>
    </form>
  </div>
</template>

<script>
  import {required, maxLength, between, decimal, integer} from 'vuelidate/lib/validators'

  import {Errors} from '../../../constants/errors'
  import {FieldsLength} from '../../../constants/fieldsLength'
  import {FieldsType} from '../../../constants/fieldsType'
  import {FieldsValueBounds} from '../../../constants/fieldsValueBounds'
  import {Messages} from '../../../constants/messages'
  import {Url} from '../../../constants/url'

  export default {
    name: 'CarEditContent',

    data: () => ({
      errors: Errors,
      fieldsLength: FieldsLength,
      fieldsValueBounds: FieldsValueBounds,
      fieldsType: FieldsType,
      messages: Messages,

      number: null,
      fuelConsumption: null,
      loadCapacity: null,
      carType: null,

      sending: false,
      carUpdated: false,
      hasError: false,
      errorMessage: null
    }),

    validations: {
      number: {
        required,
        maxLength: maxLength(FieldsLength.CAR_NUMBER)
      },
      fuelConsumption: {
        required,
        decimal,
        between: between(FieldsValueBounds.CAR_FUEL_CONSUMPTION.min, FieldsValueBounds.CAR_FUEL_CONSUMPTION.max)
      },
      loadCapacity: {
        required,
        integer,
        between: between(FieldsValueBounds.CAR_LOAD_CAPACITY.min, FieldsValueBounds.CAR_LOAD_CAPACITY.max)
      }
    },

    methods: {
      getValidationClass(fieldName) {
        const field = this.$v[fieldName]
        if (field) {
          return {
            'md-invalid': field.$invalid && field.$dirty
          }
        }
      },

      validateCar() {
        this.$v.$touch()
        if (!this.$v.$invalid) {
          this.updateCar()
        }
      },

      updateCar() {
        this.sending = true
        const form = {
          number: this.number,
          fuelConsumption: this.fuelConsumption,
          loadCapacity: this.loadCapacity,
          carType: this.carType
        }
        this.$http.put(`${Url.CAR}/${this.$route.params.id}`, JSON.stringify(form), {
          headers: {
            Authorization: `Bearer ${localStorage.accessToken}`
          }
        })
          .then(() => {
            this.carUpdated = true
            this.sending = false
            this.hasError = false
            this.$v.$reset()
          }, response => {
            this.hasError = true
            this.sending = false
            this.errorMessage = response.body.errors[0]
          })
      }
    },

    mounted: function() {
      const userRoles = JSON.parse(localStorage.getItem('roles'))
      if (!userRoles || !userRoles.includes('ADMIN')) {
        this.$router.replace('/')
      }

      this.$http.get(`${Url.CAR}/${this.$route.params.id}`, {
        headers: {
          Authorization: `Bearer ${localStorage.accessToken}`
        }
      })
        .then(response => {
          this.number = response.body.number
          this.fuelConsumption = response.body.fuelConsumption
          this.loadCapacity = response.body.loadCapacity
          this.carType = response.body.carType
        }, response => {
          this.hasError = true
          this.errorMessage = response.body.errors[0]
        })
    }
  }
</script>

<style scoped>

</style>
